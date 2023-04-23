package com.yam.funteer.user.service;

import com.yam.funteer.common.code.PostGroup;
import com.yam.funteer.user.dto.request.*;
import com.yam.funteer.user.dto.request.member.*;
import com.yam.funteer.user.dto.response.ChargeListResponse;
import com.yam.funteer.user.dto.response.member.GiftDetailResponse;
import com.yam.funteer.user.dto.response.member.MemberAccountResponse;
import com.yam.funteer.user.dto.response.member.MemberProfileResponse;
import com.yam.funteer.user.dto.response.member.MileageDetailResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {

    void createAccountWithOutProfile(CreateMemberRequest request);
    void setAccountSignOut(BaseUserRequest baseUserRequest);
    MemberProfileResponse getProfile(Long userId);
    void updateProfile(UpdateMemberProfileRequest request);

    MemberAccountResponse getAccountInfo();
    void updateAccount(UpdateMemberAccountRequest request);

    void followTeam(Long teamId);
    void wishFunding(Long fundingId);

    MileageDetailResponse getMileageDetails(PostGroup postGroup, Pageable pageable);
    void chargeMileage(ChargeRequest chargeRequest);

	Page<ChargeListResponse> getChargeList(Pageable pageable);

    GiftDetailResponse getGiftDetails(Pageable pageable);
}
