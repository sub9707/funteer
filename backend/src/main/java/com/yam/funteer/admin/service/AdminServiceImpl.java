package com.yam.funteer.admin.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yam.funteer.admin.dto.MemberListResponse;
import com.yam.funteer.admin.dto.TeamConfirmRequest;
import com.yam.funteer.admin.dto.TeamListResponse;
import com.yam.funteer.attach.entity.Attach;
import com.yam.funteer.attach.entity.TeamAttach;
import com.yam.funteer.attach.repository.TeamAttachRepository;
import com.yam.funteer.badge.service.BadgeService;
import com.yam.funteer.common.code.PostGroup;
import com.yam.funteer.common.code.PostType;
import com.yam.funteer.common.code.UserType;
import com.yam.funteer.exception.UserNotFoundException;
import com.yam.funteer.funding.dto.request.RejectReasonRequest;
import com.yam.funteer.funding.entity.Funding;
import com.yam.funteer.funding.entity.Report;
import com.yam.funteer.funding.exception.FundingNotFoundException;
import com.yam.funteer.funding.exception.NotFoundReportException;
import com.yam.funteer.funding.repository.FundingRepository;
import com.yam.funteer.funding.repository.ReportRepository;
import com.yam.funteer.mail.service.EmailService;
import com.yam.funteer.user.entity.Member;
import com.yam.funteer.user.entity.Team;
import com.yam.funteer.user.repository.MemberRepository;
import com.yam.funteer.user.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{
	private final MemberRepository memberRepository;
	private final TeamRepository teamRepository;
	private final TeamAttachRepository teamAttachRepository;
	private final FundingRepository fundingRepository;
	private final EmailService emailService;
	private final ReportRepository reportRepository;
	private final BadgeService badgeService;

	@Override
	public MemberListResponse findMembersWithPageable(String keyword, UserType userType, Pageable pageable) {
		Page<Member> memberPage = userType == null
			? memberRepository.findAllByNicknameContainingAndUserTypeIsNot(keyword, UserType.ADMIN, pageable)
			: memberRepository.findAllByNicknameContainingAndUserType(keyword, userType, pageable);

		return MemberListResponse.of(memberPage);
	}

	@Override
	public TeamListResponse findTeamWithPageable(String keyword, UserType userType, Pageable pageable) {
		Page<Team> teamPage = userType == null
			? teamRepository.findAllByNameContaining(keyword, pageable)
			: teamRepository.findAllByNameContainingAndUserType(keyword, userType, pageable);

		List<TeamListResponse.TeamInfo> list = teamPage.stream().map(team -> {
			team.expiredCheck();
			List<TeamAttach> teamAttachList = teamAttachRepository.findAllByTeam(team);
			String vmsFilePath = null, perFormFilePath = null;
			for(TeamAttach teamAttach : teamAttachList){
				Attach attach = teamAttach.getAttach();
				switch(attach.getFileType()){
					case VMS: vmsFilePath = attach.getPath(); break;
					case PERFORM: perFormFilePath = attach.getPath(); break;
					default: break;
				}
			};
			return TeamListResponse.TeamInfo.of(team, vmsFilePath, perFormFilePath);
		}).collect(Collectors.toList());

		return TeamListResponse.of(teamPage, list);
	}

	@Override
	public void resignMember(Long memberId) {
		Member member = memberRepository.findById(memberId).orElseThrow(UserNotFoundException::new);
		member.signOut();
	}

	@Override
	public void resignTeam(Long teamId) {
		Team team = teamRepository.findById(teamId).orElseThrow(UserNotFoundException::new);
		team.signOut();
	}

	@Override
	public void acceptTeam(Long teamId) {
		Team team = teamRepository.findById(teamId).orElseThrow(UserNotFoundException::new);
		team.accept();
	}

	@Override
	public void rejectTeam(Long teamId, TeamConfirmRequest request) {
		Team team = teamRepository.findById(teamId).orElseThrow(UserNotFoundException::new);
		emailService.sendTeamRejectMessage(team.getEmail(), request.getRejectComment());
	}


	@Override
	public void acceptFunding(Long fundingId) {
		Funding funding = fundingRepository.findByFundingId(fundingId).orElseThrow(FundingNotFoundException::new);
		funding.setPostType(PostType.FUNDING_ACCEPT);
	}

	@Override
	public void rejectFunding(Long fundingId, RejectReasonRequest data) {
		Funding funding = fundingRepository.findByFundingId(fundingId).orElseThrow(FundingNotFoundException::new);
		funding.setPostType(PostType.FUNDING_REJECT);
		funding.setRejectComment(data.getRejectReason());
		emailService.sendPostRejectMessage(funding.getTeam().getEmail(), data.getRejectReason(), PostGroup.FUNDING);
	}

	@Override
	public void acceptReport(Long fundingId) {
		log.info("fundingId => {}", fundingId);
		Funding funding = fundingRepository.findByFundingId(fundingId).orElseThrow(FundingNotFoundException::new);
		log.info("funding => {}", funding);
		Team team = teamRepository.findById(funding.getTeam().getId()).orElseThrow(UserNotFoundException::new);
		log.info("team => {}", team);
		team.updateLastActivity();
		team.addTotalFundingAmount(funding.getCurrentFundingAmount());
		funding.setPostType(PostType.REPORT_ACCEPT);
		badgeService.teamFundingBadges(funding.getTeam());
	}

	@Override
	public void rejectReport(Long fundingId, RejectReasonRequest data) {
		Funding funding = fundingRepository.findByFundingId(fundingId).orElseThrow(FundingNotFoundException::new);
		Report report = reportRepository.findByFundingFundingId(fundingId).orElseThrow(NotFoundReportException::new);
		funding.setPostType(PostType.REPORT_REJECT);
		report.setReportRejectComment(data.getRejectReason());
		emailService.sendPostRejectMessage(funding.getTeam().getEmail(), data.getRejectReason(), PostGroup.REPORT);
	}

}
