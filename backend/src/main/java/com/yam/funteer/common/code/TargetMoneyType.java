package com.yam.funteer.common.code;

import com.yam.funteer.common.code.TypeModel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter @ToString
@RequiredArgsConstructor
public enum TargetMoneyType implements TypeModel {
	LEVEL_ONE("1단계"),
	LEVEL_TWO("2단계"),
	LEVEL_THREE("3단계");
	private final String description;

	@Override
	public String getKey() {
		return name();
	}
}
