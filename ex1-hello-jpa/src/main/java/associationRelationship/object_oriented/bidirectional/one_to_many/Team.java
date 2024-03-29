package associationRelationship.object_oriented.bidirectional.one_to_many;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

//@Entity
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "team_id")
	private Long id;

	private String name;

	@OneToMany // 1이 연관 관계의 주인인 경우
	@JoinColumn(name = "team_id") // DB에서 조인에 활용되는 컬럼
	private List<Member> members = new ArrayList<>(); // 관례대로 ArrayList로 초기화 해둔다.

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

	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
	}
}
