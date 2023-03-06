package hellojpa;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
//@Table(name = "USER") // USER라는 테이블에 매핑된다.
public class Member {

	@Id // PK 설정
	private Long id;

//	@Column(name = "username") // username이라는 컬럼에 매핑된다.
	private String name;

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
}
