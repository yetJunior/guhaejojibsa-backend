package mutsa.api.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import mutsa.common.domain.models.report.Report;
import mutsa.common.domain.models.report.ReportStatus;

@Data
@AllArgsConstructor
public class ReportResponseDto {
    private String apiId;
    private String reporterName;
    private String reportedName;
    private String content;
    private ReportStatus status;

    public static ReportResponseDto of(Report report) {
        return new ReportResponseDto(
                report.getApiId(),
                report.getReporter().getUsername(),
                report.getReportedUser().getUsername(),
                report.getContent(),
                report.getStatus()
        );
    }
}
