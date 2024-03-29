package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

	private String username;
	private int age;

	@QueryProjection // <- compileQuerydsl => Dto 생성자를 Q타입으로 생성
	public MemberDto(String username, int age) {
		this.username = username;
		this.age = age;
	}
}
