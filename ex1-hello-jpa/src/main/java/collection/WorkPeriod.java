package collection;

import java.time.LocalDateTime;
import javax.persistence.Embeddable;

@Embeddable
public class WorkPeriod {

	private LocalDateTime startDate;
	private LocalDateTime endDate;


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
