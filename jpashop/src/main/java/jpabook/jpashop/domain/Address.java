package jpabook.jpashop.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Address {

	@Column(length = 10)
	private String city;
	@Column(length = 10)
	private String street;
	@Column(length = 5)
	private String zipcode;

	// 의미있는 메소드 가능
	public String fullAddress() {
		return getCity() + " " + getStreet() + " " + getZipcode();
	}

	public Address() {
	}

	public String getCity() {
		return city;
	}

	private void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	private void setStreet(String street) {
		this.street = street;
	}

	public String getZipcode() {
		return zipcode;
	}

	private void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	// getter를 사용해야 프록시를 사용할 수 있다.
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Address address = (Address) o;
		return Objects.equals(getCity(), address.getCity()) && Objects.equals(
				getStreet(), address.getStreet()) && Objects.equals(getZipcode(),
				address.getZipcode());
	}

	// getter를 사용해야 프록시를 사용할 수 있다.
	@Override
	public int hashCode() {
		return Objects.hash(getCity(), getStreet(), getZipcode());
	}
}
