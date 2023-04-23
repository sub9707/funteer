package com.yam.funteer.notice.dto.response;

import java.util.List;
import java.util.Map;

import com.yam.funteer.donation.entity.Donation;
import com.yam.funteer.notice.entity.Notice;
import com.yam.funteer.post.entity.Post;

import lombok.Getter;

@Getter
public class NoticeBaseRes {
	private Long id;
	private String title;
	private String content;
	private List<Map.Entry<String,String>> files;

	public NoticeBaseRes(Notice entity,List<Map.Entry<String,String>>files){
		this.id=entity.getNoticeId();
		this.title=entity.getTitle();
		this.content=entity.getContent();
		this.files=files;
	}
}
