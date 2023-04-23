package com.yam.funteer.donation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yam.funteer.alarm.service.AlarmService;
import com.yam.funteer.attach.entity.Attach;
import com.yam.funteer.attach.entity.PostAttach;
import com.yam.funteer.attach.repository.AttachRepository;
import com.yam.funteer.attach.repository.PostAttachRepository;
import com.yam.funteer.badge.service.BadgeService;
import com.yam.funteer.common.aws.AwsS3Uploader;
import com.yam.funteer.common.code.PostGroup;
import com.yam.funteer.common.code.PostType;
import com.yam.funteer.common.code.UserType;
import com.yam.funteer.common.security.SecurityUtil;
import com.yam.funteer.donation.dto.request.DonationModifyReq;
import com.yam.funteer.donation.dto.request.DonationRegisterReq;
import com.yam.funteer.donation.dto.response.DonationAdminListRes;
import com.yam.funteer.donation.dto.response.DonationBaseRes;
import com.yam.funteer.donation.dto.response.DonationListRes;
import com.yam.funteer.donation.entity.Donation;
import com.yam.funteer.donation.exception.DonationNotFoundException;
import com.yam.funteer.donation.exception.DonationPayException;
import com.yam.funteer.donation.repository.DonationRepository;
import com.yam.funteer.donation.dto.request.DonationJoinReq;

import com.yam.funteer.exception.UserNotFoundException;

import com.yam.funteer.pay.entity.Payment;
import com.yam.funteer.pay.repository.PaymentRepository;

import com.yam.funteer.user.entity.User;
import com.yam.funteer.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService{

	private final PaymentRepository paymentRepository;
	private final DonationRepository donationRepository;
	private final UserRepository userRepository;
	private final AwsS3Uploader awsS3Uploader;
	private final AttachRepository attachRepository;
	private final PostAttachRepository postAttachRepository;
	private final AlarmService alarmService;
	private final BadgeService badgeService;

	public Page<Donation> donationGetList(int page,int size) {
		PageRequest pageRequest=PageRequest.of(page,size);
		Page<Donation> donations=donationRepository.findAllByPostTypeOrderByDonationIdDesc(PostType.DONATION_CLOSE,pageRequest);

		return donations;
	}

	public Page<Donation> donationGetAdminList(int page,int size) {
		PageRequest pageRequest=PageRequest.of(page,size);
		Page<Donation>donations=donationRepository.findAllByOrderByDonationIdDesc(pageRequest);

		return donations;
	}

	public  DonationBaseRes donationGetDetail(Long donationId) {
		Donation donation=donationRepository.findByDonationId(donationId).orElseThrow(()->new DonationNotFoundException());
		PostAttach postAttach=postAttachRepository.findFirstByPost(donation);

		return new DonationBaseRes(donation,postAttach.getAttach().getPath());
	}

	@Override
	public DonationBaseRes donationGetCurrent() {
		Donation donation=donationRepository.findFirstByOrderByDonationIdDesc().orElseThrow(DonationNotFoundException::new);
		return donationGetDetail(donation.getDonationId());
	}

	public Payment donationJoin(Long postId, DonationJoinReq donationJoinReq) {
		Donation donation=donationRepository.findByDonationId(postId).orElseThrow(DonationNotFoundException::new);
		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());
		Long paymentAmount=Long.parseLong(donationJoinReq.getPaymentAmount());
		if(user.getMoney()<paymentAmount||user.getMoney()==0){
			throw new DonationPayException();
		}
		Payment payment=donationJoinReq.toPaymentEntity(paymentAmount,user,donation);

		paymentRepository.save(payment);
		donation.currentAmountUpdate(paymentAmount);
		user.charge(-paymentAmount);

		badgeService.totalPayAmount(user);
		badgeService.postBadges(user,PostGroup.DONATION);

		return payment;
	}

	public DonationBaseRes donationRegister(DonationRegisterReq donationRegisterReq) {
		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());

		if(user.getUserType().equals(UserType.ADMIN)) {
			Donation donation=donationRepository.save(donationRegisterReq.toEntity());
			List<String>attachList=new ArrayList<>();

			MultipartFile file=donationRegisterReq.getFile();

			String fileUrl = awsS3Uploader.upload(file, "donation");
			Attach attach = donationRegisterReq.toAttachEntity(fileUrl, file.getOriginalFilename());
			PostAttach postAttach = PostAttach.builder()
				.attach(attach)
				.post(donation)
				.build();
			attachList.add(fileUrl);
			attachRepository.save(attach);
			postAttachRepository.save(postAttach);

			return new DonationBaseRes(donation,fileUrl);
		}else throw new IllegalArgumentException("접근 권한이 없습니다.");
	}

	@Override
	public void donationStatusModify(Long donationId, PostType postType) {
		Donation donationOrigin=donationRepository.findByDonationId(donationId).orElseThrow(()->new DonationNotFoundException());
		if(postType.equals(PostType.DONATION_ACTIVE)){
			Donation donation=Donation.builder()
				.startDate(donationOrigin.getStartDate())
				.title(donationOrigin.getTitle())
				.amount(donationOrigin.getAmount())
				.currentAmount(donationOrigin.getCurrentAmount())
				.content(donationOrigin.getContent())
				.endDate(LocalDate.now())
				.donationId(donationId)
				.id(donationOrigin.getId()).postGroup(PostGroup.DONATION).postType(PostType.DONATION_CLOSE).build();
			donationRepository.save(donation);
		}

	}

	// public DonationBaseRes donationDelete(Long postId) throws DonationNotFoundException{
	// 	User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());
	// 	Donation donation = donationRepository.findById(postId).orElseThrow(() -> new DonationNotFoundException());
	// 	if(user.getUserType().equals(UserType.ADMIN)) {
	// 		donationRepository.delete(donation);
	// 	}else throw new IllegalArgumentException();
	// }


	public DonationBaseRes donationModify(Long donationId, DonationModifyReq donationModifyReq){
		Donation donationOrigin=donationRepository.findByDonationId(donationId).orElseThrow(() -> new DonationNotFoundException());

		User user=userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(()->new UserNotFoundException());
		if(user.getUserType().equals(UserType.ADMIN)) {
			Donation donation=donationRepository.save(
				donationModifyReq.toEntity(donationOrigin.getId(),donationId,donationOrigin.getCurrentAmount(),donationOrigin.getStartDate())
			);

			List<PostAttach>postAttachList=postAttachRepository.findAllByPost(donationOrigin);
			for(PostAttach postAttach:postAttachList){
				awsS3Uploader.delete("donation/",postAttach.getAttach().getPath());
				postAttachRepository.deleteById(postAttach.getId());
				attachRepository.deleteById(postAttach.getAttach().getId());
			}

			MultipartFile file=donationModifyReq.getFile();

			String fileUrl = awsS3Uploader.upload(file, "donation");
			Attach attach = donationModifyReq.toAttachEntity(fileUrl, file.getOriginalFilename());
			PostAttach postAttach = PostAttach.builder()
				.attach(attach)
				.post(donation)
				.build();

			attachRepository.save(attach);
			postAttachRepository.save(postAttach);

			if(donationModifyReq.getPostType().equals(PostType.DONATION_CLOSE)){
				donation.setEndDate();
				List<Payment>paymentList=paymentRepository.findAllByPost(donation);
				Set<User> userList;
				userList=paymentList.stream().map(Payment::getUser).collect(Collectors.toSet());
				List<String> userEmailList=userList.stream().map(User::getEmail).collect(Collectors.toList());
				alarmService.sendList(userEmailList,donation.getTitle()+" 종료되었습니다. 참여감사띠~!~!","/donation/"+donationId);
			}
			return new DonationBaseRes(donation,fileUrl);
		}else throw new IllegalArgumentException("접근 권한이 없습니다.");
	}
}

