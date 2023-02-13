package ru.ivolkov.asterisk.clients.dto;

import lombok.Data;

@Data
public class ExtensionStatusDto {

	private String exten;
	private Integer status;
	private String statusText;

}
