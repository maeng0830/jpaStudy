package embedded;

import java.time.LocalDateTime;
import java.util.Objects;

//@Embeddable
public class Period {

	private LocalDateTime startDate;
	private LocalDateTime endDate;

	// 임베디드 타입은 기본 생성자를 갖고 있어야 한다.
	public Period() {
	}

	public Period(LocalDateTime startDate, LocalDateTime endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	private void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	private void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Period that = (Period) o;
		return Objects.equals(getStartDate(), that.getStartDate())
				&& Objects.equals(getEndDate(), that.getEndDate());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStartDate(), getEndDate());
	}
}
