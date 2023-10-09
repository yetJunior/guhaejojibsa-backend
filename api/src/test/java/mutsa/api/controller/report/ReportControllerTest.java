package mutsa.api.controller.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.dto.report.ReportRegisterDto;
import mutsa.api.dto.report.ReportResponseDto;
import mutsa.api.dto.report.ReportUpdateStatusDto;
import mutsa.api.service.report.ReportService;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.models.report.Report;
import mutsa.common.domain.models.report.ReportStatus;
import mutsa.common.domain.models.user.User;
import mutsa.common.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    private static MockedStatic<SecurityUtil> securityUtilMockedStatic;

    private User reporter, reportedUser;

    @BeforeAll
    public static void beforeAll() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtilMockedStatic.close();
    }

    @BeforeEach
    public void setup() {
        initializeEntities();
        setupMocks();
    }

    private void initializeEntities() {
        reporter = User.of("reporter", "password", "reporterEmail@", "reporterOauthName", null, "reporter");
        reporter = userRepository.save(reporter);

        reportedUser = User.of("reported", "password", "reportedEmail@", "reportedOauthName", null, "reported");
        reportedUser = userRepository.save(reportedUser);
    }

    private void setupMocks() {
        when(SecurityUtil.getCurrentUsername()).thenReturn(reporter.getUsername());

        ReportResponseDto mockResponseDto = ReportResponseDto.of(
                Report.of(reporter, reportedUser, "Test content")
        );
        when(reportService.getReport(anyString())).thenReturn(mockResponseDto);
    }

    @DisplayName("신고 등록")
    @Test
    void registerReport() throws Exception {
        ReportRegisterDto requestDto = new ReportRegisterDto();
        requestDto.setResourceType("article");
        requestDto.setResourceApiId("resourceApiId");
        requestDto.setContent("Report Content");

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @DisplayName("모든 신고 조회 (관리자용)")
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReports() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // 특정 신고 조회 테스트
    @DisplayName("특정 신고 조회 (관리자용)")
    @Test
    @WithMockUser(roles = "ADMIN")
    void getReport() throws Exception {
        String reportApiId = "testApiId";
        mockMvc.perform(get("/api/reports/" + reportApiId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // 특정 신고 조회 (관리자용)
    @DisplayName("특정 신고 조회 (관리자용) 상태 필터 적용")
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReportsWithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("status", ReportStatus.PENDING.name()))
                .andExpect(status().isOk());
    }

    @DisplayName("신고 상태 업데이트 (관리자용)")
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateReportStatus() throws Exception {
        String reportApiId = "testApiId";
        ReportUpdateStatusDto updateDto = new ReportUpdateStatusDto();
        updateDto.setStatus(ReportStatus.RESOLVED);

        mockMvc.perform(put("/api/reports/" + reportApiId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @DisplayName("신고 삭제 (관리자용)")
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReport() throws Exception {
        String reportApiId = "testApiId";

        mockMvc.perform(delete("/api/reports/" + reportApiId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
