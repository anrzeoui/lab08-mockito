package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final String STUDENT_ID = "B242270129";
    private static final String COURSE_ID = "CS313";
    private static final String EMAIL = "student@example.edu";

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;

    private CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student activeStudent() {
        return new Student(STUDENT_ID, EMAIL, true, List.of("CS201"));
    }

    private Student inactiveStudent() {
        return new Student(STUDENT_ID, EMAIL, false, List.of("CS201"));
    }

    private void stubEligibleStudent() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));
    }

    // A1: when/thenReturn + verify
    @Test
    void register_activeStudentWithPrerequisites_savesAndEmails() {
        stubEligibleStudent();

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), eq("Бүртгэл баталгаажлаа"), contains(COURSE_ID));
    }

    // A2: never() for unknown student
    @Test
    void register_missingStudent_returnsNoStudentWithoutSideEffects() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register(STUDENT_ID, COURSE_ID));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3: verifyNoInteractions for inactive student
    @Test
    void register_inactiveStudent_returnsErrorWithoutEmailInteraction() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(inactiveStudent());

        assertEquals("ERROR_INACTIVE_STUDENT", service.register(STUDENT_ID, COURSE_ID));
        verifyNoInteractions(emailSender);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A4: missing prerequisite prevents save
    @Test
    void register_missingPrerequisite_doesNotSaveRegistration() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201", "CS302"));

        assertEquals("ERROR_PREREQUISITES", service.register(STUDENT_ID, COURSE_ID));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A5: doThrow on mail service must not undo the registration
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSavesRegistration() {
        stubEligibleStudent();
        doThrow(new RuntimeException("SMTP temporarily down"))
                .when(emailSender).send(eq(EMAIL), anyString(), anyString());

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), contains(COURSE_ID));
    }

    // A6: both arguments checked separately (also detects mutant #3)
    @Test
    void register_nullStudentIdOrCourseId_throwsWithoutDependencyCalls() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, COURSE_ID));
        assertThrows(IllegalArgumentException.class, () -> service.register(STUDENT_ID, null));
        verifyNoInteractions(repository, emailSender);
    }

    // A7: ArgumentCaptor checks the outgoing email body
    @Test
    void register_successfulRegistration_emailBodyContainsCourseId() {
        stubEligibleStudent();
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));
        verify(emailSender).send(eq(EMAIL), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID),
                "Email body should contain the registered course code");
    }

    // A8: 3 parameterized cases; only needed stubs are configured, no lenient() needed.
    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource({
        "MISSING, ERROR_NO_STUDENT",
        "INACTIVE, ERROR_INACTIVE_STUDENT",
        "NO_COURSE, ERROR_NO_COURSE"
    })
    void register_invalidScenario_returnsExpectedErrorWithoutSaving(String scenario, String expected) {
        switch (scenario) {
            case "MISSING" -> when(repository.findStudent(STUDENT_ID)).thenReturn(null);
            case "INACTIVE" -> when(repository.findStudent(STUDENT_ID)).thenReturn(inactiveStudent());
            case "NO_COURSE" -> {
                when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
                when(repository.courseExists(COURSE_ID)).thenReturn(false);
            }
            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }

        assertEquals(expected, service.register(STUDENT_ID, COURSE_ID));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}
