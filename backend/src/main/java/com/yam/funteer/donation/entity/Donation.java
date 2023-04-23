package com.yam.funteer.donation.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.yam.funteer.common.code.PostType;
import com.yam.funteer.post.entity.Post;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name="donation")
@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Donation extends Post {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(insertable = false,updatable = false,nullable = false,unique = true )
	private Long donationId;

	private @NotNull Long amount;
	private Long currentAmount;

	private LocalDate startDate;
	private LocalDate endDate;

	public void currentAmountUpdate(Long payAmount){
		this.currentAmount+=payAmount;
	}
	public void setEndDate(){
		this.endDate= LocalDate.now();
	}

}
