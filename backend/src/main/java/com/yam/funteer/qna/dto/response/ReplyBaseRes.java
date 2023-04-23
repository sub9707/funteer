package com.yam.funteer.qna.dto.response;

import java.time.LocalDateTime;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yam.funteer.alarm.entity.Alarm;
import com.yam.funteer.qna.entity.Qna;
import com.yam.funteer.qna.entity.Reply;

import lombok.Getter;

@Getter
public class ReplyBaseRes {
	private Long id;
	private Long qnaId;
	private String content;
	private LocalDateTime regDate;

	public ReplyBaseRes(Reply entity) {
		this.id = entity.getId();
		this.qnaId=entity.getQna().getQnaId();
		this.content=entity.getContent();
		this.regDate=entity.getRegDate();
	}

}
