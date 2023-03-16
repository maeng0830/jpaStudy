package embedded;

import java.util.Objects;
import javax.persistence.Embeddable;

//@Embeddable
public class Address {

	private String city;
	private String street;
	private String zipcode;

	// 임베디드 타입은 기본 생성자를 갖고 있어야 한다.
	public Address() {
	}

	public Address(String city, String street, String zipcode) {
		this.city = city;
		this.street = street;
		this.zipcode = zipcode;
	}

	public String getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public String getZipcode() {
		return zipcode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Address address = (Address) o;
		return Objects.equals(city, address.city) && Objects.equals(street,
				address.street) && Objects.equals(zipcode, address.zipcode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(city, street, zipcode);
	}
}
