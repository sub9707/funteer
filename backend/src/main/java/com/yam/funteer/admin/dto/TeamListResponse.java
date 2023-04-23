package com.yam.funteer.admin.dto;

import com.yam.funteer.attach.entity.Attach;
import com.yam.funteer.attach.entity.TeamAttach;
import com.yam.funteer.common.code.UserType;
import com.yam.funteer.user.entity.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@ToString @Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamListResponse {
	private long totalElements;
	private int totalPages;
	private List<TeamInfo> userList;

	public static TeamListResponse of(Page<Team> teamPage, List<TeamInfo> userList) {
		return new TeamListResponse(teamPage.getTotalElements(), teamPage.getTotalPages(), userList);
	}


	@Builder
	@Getter @Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TeamInfo{
		private Long id;
		private String name;
		private String email;
		private String phone;
		private String vmsFileUrl;
		private String performFileUrl;
		private LocalDate lastActivity;
		private UserType userType;

		public static TeamInfo of(Team team, String vmsFilePath, String perFormFilePath) {
			TeamInfo teamInfo = TeamInfo.builder()
				.id(team.getId())
				.email(team.getEmail())
				.name(team.getName())
				.phone(team.getPhone())
				.vmsFileUrl(vmsFilePath)
				.performFileUrl(perFormFilePath)
				.userType(team.getUserType())
				.build();
			if(team.getLastActivity() != null){
				teamInfo.setLastActivity(team.getLastActivity().toLocalDate());
			}
			return teamInfo;
		}
	}
}
