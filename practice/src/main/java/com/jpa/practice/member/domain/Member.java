package com.jpa.practice.member.domain;

import com.jpa.practice.team.domain.TeamForEager;
import com.jpa.practice.team.domain.TeamForLazy;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Member {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;

	private Integer age;

	@ManyToOne(fetch = FetchType.EAGER)
	private TeamForEager teamForEager;

	@ManyToOne(fetch = FetchType.LAZY)
	private TeamForLazy teamForLazy;

	@Builder
	public Member(Long id, String username, Integer age, TeamForEager teamForEager,
				  TeamForLazy teamForLazy) {
		this.id = id;
		this.username = username;
		this.age = age;
		this.teamForEager = teamForEager;
		this.teamForLazy = teamForLazy;
	}
}
