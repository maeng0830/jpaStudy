package embedded;

import java.time.LocalDateTime;
import javax.persistence.Embeddable;

//@Embeddable
public class WorkPeriod {

	private LocalDateTime startDate;
	private LocalDateTime endDate;

	// 임베디드 타입은 기본 생성자를 갖고 있어야 한다.
	public WorkPeriod() {
	}

	public WorkPeriod(LocalDateTime startDate, LocalDateTime endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
}
