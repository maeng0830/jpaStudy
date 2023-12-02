package com.jpa.practice.team.domain;

import com.jpa.practice.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class TeamForLazy {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(mappedBy = "teamForLazy")
	private List<Member> members = new ArrayList<>();

	@Builder
	public TeamForLazy(Long id, String name, List<Member> members) {
		this.id = id;
		this.name = name;
		this.members = members;
	}
}
