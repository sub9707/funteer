package com.yam.funteer.qna.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yam.funteer.alarm.service.AlarmService;
import com.yam.funteer.attach.entity.Attach;
import com.yam.funteer.attach.entity.PostAttach;
import com.yam.funteer.attach.repository.AttachRepository;
import com.yam.funteer.attach.repository.PostAttachRepository;
import com.yam.funteer.common.aws.AwsS3Uploader;
import com.yam.funteer.common.code.UserType;
import com.yam.funteer.common.security.SecurityUtil;
import com.yam.funteer.exception.UserNotFoundException;

import com.yam.funteer.qna.dto.request.QnaRegisterReq;
import com.yam.funteer.qna.dto.response.QnaBaseRes;
import com.yam.funteer.qna.dto.response.QnaListRes;
import com.yam.funteer.qna.entity.Qna;
import com.yam.funteer.qna.entity.Reply;
import com.yam.funteer.qna.exception.QnaNotFoundException;
import com.yam.funteer.qna.exception.ReplyNotFoundException;
import com.yam.funteer.qna.repository.QnaRepository;
import com.yam.funteer.qna.repository.ReplyRepository;

import com.yam.funteer.user.entity.User;
import com.yam.funteer.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaServiceImpl implements QnaService {

	private final QnaRepository qnaRepository;
	private final UserRepository userRepository;
	private final ReplyRepository replyRepository;
	private final PostAttachRepository postAttachRepository;
	private final AttachRepository attachRepository;

	private final AwsS3Uploader awsS3Uploader;
	private final AlarmService alarmService;

	@Override
	public List<QnaListRes> qnaGetList(int page,int size) {
		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());
		List<QnaListRes>list;
		PageRequest pageRequest=PageRequest.of(page,size);
		if(user.getUserType().equals(UserType.ADMIN)){
			List<Qna>qnaList=qnaRepository.findAllByOrderByQnaIdDesc(pageRequest);
			list=qnaList.stream().map(qna->new QnaListRes(qna)).collect(Collectors.toList());

			return list;
		}

		List<Qna>qnaList=qnaRepository.findAllByUserOrderByQnaIdDesc(user,pageRequest);
		list=qnaList.stream().map(qna->new QnaListRes(qna)).collect(Collectors.toList());

		return list;
	}

	@Override
	public QnaBaseRes qnaRegister(QnaRegisterReq qnaRegisterReq){
		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());
		Qna qna=qnaRepository.save(qnaRegisterReq.toEntity(user));

		Map<String,String>attachList=new HashMap<>();
		List<MultipartFile>files=qnaRegisterReq.getFiles();
		if(!files.isEmpty()){
			for(MultipartFile file:files) {
				if(file.isEmpty())break;

				String fileUrl = awsS3Uploader.upload(file, "qna");
				Attach attach = qnaRegisterReq.toAttachEntity(fileUrl, file.getOriginalFilename());
				PostAttach postAttach = PostAttach.builder()
					.attach(attach)
					.post(qna)
					.build();
				attachList.put(file.getOriginalFilename(),fileUrl);
				attachRepository.save(attach);
				postAttachRepository.save(postAttach);
			}
		}
		List<Map.Entry<String,String>> pathList=attachList.entrySet().stream().collect(Collectors.toList());
		List<User> adminList = userRepository.findAllByUserType(UserType.ADMIN);
		List<String>adminEmailList=adminList.stream().map(User::getEmail).collect(Collectors.toList());
		alarmService.sendList(adminEmailList,qna.getTitle()+", QnA가 등록되었습니다.", "/qna");
		return new QnaBaseRes(qna,pathList);
	}

	@Override
	public QnaBaseRes qnaGetDetail(Long qnaId) {
		Qna qna = qnaRepository.findByQnaId(qnaId).orElseThrow(() -> new QnaNotFoundException());
		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());

		if (qna.getUser().getId()==user.getId()||user.getUserType().equals(UserType.ADMIN)) {
			List<PostAttach>postAttachList=postAttachRepository.findAllByPost(qna);
			Map<String,String>attachList=new HashMap<>();

			if(postAttachList.size()>0) {
				for (PostAttach postAttach : postAttachList) {
					attachList.put(postAttach.getAttach().getName(),postAttach.getAttach().getPath());
				}
			}
			List<Map.Entry<String,String>> pathList=attachList.entrySet().stream().collect(Collectors.toList());
			return new QnaBaseRes(qna,pathList);
		}
		else throw new IllegalArgumentException("접근권한이 없습니다.");
	}

	@Override
	public QnaBaseRes qnaModify(Long qnaId, QnaRegisterReq qnaRegisterReq){
		Qna qna = qnaRepository.findByQnaId(qnaId).orElseThrow(() -> new QnaNotFoundException());
		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());
		if(replyRepository.findByQna(qna).isPresent()){
			throw new IllegalArgumentException("이미 답변이 완료된 글입니다");
		}

		if(user.getId()==qna.getUser().getId()) {
			qnaRepository.save(qnaRegisterReq.toEntity(user,qna.getId(),qnaId));
			List<PostAttach>postAttachList=postAttachRepository.findAllByPost(qna);
			for(PostAttach postAttach:postAttachList){
				awsS3Uploader.delete("qna/",postAttach.getAttach().getPath());
				postAttachRepository.deleteById(postAttach.getId());
				attachRepository.deleteById(postAttach.getAttach().getId());
			}

			List<MultipartFile>files=qnaRegisterReq.getFiles();
			Map<String,String>attachList=new HashMap<>();
			if(!files.isEmpty()){
				for(MultipartFile file:files) {
					if(file.isEmpty())break;
					String fileUrl = awsS3Uploader.upload(file, "qna");
					Attach attach = qnaRegisterReq.toAttachEntity(fileUrl, file.getOriginalFilename());
					PostAttach postAttach = PostAttach.builder()
						.attach(attach)
						.post(qna)
						.build();
					attachList.put(file.getOriginalFilename(),fileUrl);
					attachRepository.save(attach);
					postAttachRepository.save(postAttach);
				}
			}
			List<Map.Entry<String,String>> pathList=attachList.entrySet().stream().collect(Collectors.toList());
			return new QnaBaseRes(qna,pathList);
		}
		else throw new IllegalArgumentException("접근권한이 없습니다.");
	}

	@Override
	public void qnaDelete(Long qnaId){
		Qna qna = qnaRepository.findByQnaId(qnaId).orElseThrow(() -> new QnaNotFoundException());
		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());
		if(qna.getUser().getId()==user.getId()) {
			List<PostAttach>postAttachList=postAttachRepository.findAllByPost(qna);
			for(PostAttach postAttach:postAttachList){
				awsS3Uploader.delete("qna/",postAttach.getAttach().getPath());
				postAttachRepository.deleteById(postAttach.getId());
				attachRepository.deleteById(postAttach.getAttach().getId());
			}

			qnaRepository.delete(qna);
			if(replyRepository.findByQna(qna).isPresent()){
				replyRepository.delete(replyRepository.findByQna(qna).orElseThrow(ReplyNotFoundException::new));
			}

		}
		else throw new IllegalArgumentException("접근권한이 없습니다.");
	}
}
