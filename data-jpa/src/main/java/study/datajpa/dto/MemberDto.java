package study.datajpa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class MemberDto {

	private Long id;
	private String username;
	private String teamName;
}
