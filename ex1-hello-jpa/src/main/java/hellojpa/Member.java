package hellojpa;

import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * @Entity가 적용된 클래스는 JPA가 관리한다. Entity라고 부른다.
 * Entity는 JPA를 통해 DB 테이블과 매핑될 클래스이다.
 */
@Entity
//@Table(name = "USER") // USER라는 테이블에 매핑된다.
public class Member {

	@Id // PK 설정
	private Long id;

    @Column(name = "name") // name이라는 컬럼에 매핑된다.
	private String username;

	@Enumerated(EnumType.STRING)
	private RoleType rolType;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModifiedDate;

	@Lob
	private String description;

	public Member() {
	}

	public Member(Long id, String username) {
		this.id = id;
		this.username = username;
	}

	public Member(Long id, String username, RoleType rolType, Date createDate,
				  Date lastModifiedDate, String description) {
		this.id = id;
		this.username = username;
		this.rolType = rolType;
		this.createDate = createDate;
		this.lastModifiedDate = lastModifiedDate;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public RoleType getRolType() {
		return rolType;
	}

	public void setRolType(RoleType rolType) {
		this.rolType = rolType;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
