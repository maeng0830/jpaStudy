package mappedsuperclass;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "member_id")
	private Long id;

	@Column(name = "username")
	private String name;


	@ManyToOne // N이 연관관계의 주인인 경우
	@JoinColumn(name = "team_id") // DB에서 조인에 활용되는 컬럼
	private Team team;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	// 양방향 연관관계 시, 연관관계 편의 메소드를 사용하자
	public void changeTeam(Team team) {
		this.team = team;
		team.getMembers().add(this);
	}
}
