
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.impl.DBServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = com.LetucOJ.practice.PracticeApplication.class)
class DBServiceTest {

    @Mock
    private MybatisRepos mybatisRepos;

    @InjectMocks
    private DBServiceImpl dbService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProblemCaseSuccess() {
        BasicInfoServiceDTO dto = new BasicInfoServiceDTO();
        dto.setId(String.valueOf(1));
        dto.setType("SELECT");
        dto.setSubType("SINGLE_LINE");

        BasicInfoDTO mybatisResult = new BasicInfoDTO();
        when(mybatisRepos.getBasicInfoSingleCase(dto.getId())).thenReturn(mybatisResult);

        BasicInfoVO result = dbService.BasicDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 1, result.getType()); // 使用getType()
        assertEquals(mybatisResult, result.getData()); // 使用getData()
        assertNull(result.getMessage()); // 使用getMessage()
    }

    @Test
    void testGetProblemCaseNotFound() {
        BasicInfoServiceDTO dto = new BasicInfoServiceDTO();
        dto.setId(String.valueOf(1));
        dto.setType("SELECT");
        dto.setSubType("SINGLE_LINE");

        when(mybatisRepos.getBasicInfoSingleCase(dto.getId())).thenReturn(null);

        BasicInfoVO result = dbService.BasicDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Problem not found in Mybatis", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testGetProblemCaseException() {
        BasicInfoServiceDTO dto = new BasicInfoServiceDTO();
        dto.setId(String.valueOf(1));
        dto.setType("SELECT");
        dto.setSubType("SINGLE_LINE");

        when(mybatisRepos.getBasicInfoSingleCase(dto.getId())).thenThrow(new RuntimeException("Mocked Exception"));

        BasicInfoVO result = dbService.BasicDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 1, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Mybatis Error: Mocked Exception", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testInsertProblemSuccess() {
        FullInfoServiceDTO dto = new FullInfoServiceDTO();
        dto.setType("INSERT");
        dto.setSubType("SINGLE_LINE");
        dto.setData(new FullInfoDTO());

        when(mybatisRepos.insertProblem(dto.getData())).thenReturn(1);

        FullInfoVO result = dbService.FullDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 2, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertNull(result.getMessage()); // 使用getMessage()
    }

    @Test
    void testInsertProblemFailure() {
        FullInfoServiceDTO dto = new FullInfoServiceDTO();
        dto.setType("INSERT");
        dto.setSubType("SINGLE_LINE");
        dto.setData(new FullInfoDTO());

        when(mybatisRepos.insertProblem(dto.getData())).thenReturn(null);

        FullInfoVO result = dbService.FullDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Mybatis return not 1 or is null", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testInsertProblemException() {
        FullInfoServiceDTO dto = new FullInfoServiceDTO();
        dto.setType("INSERT");
        dto.setSubType("SINGLE_LINE");
        dto.setData(new FullInfoDTO());

        when(mybatisRepos.insertProblem(dto.getData())).thenThrow(new RuntimeException("Mocked Exception"));

        FullInfoVO result = dbService.FullDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Mybatis Error: Mocked Exception", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testUpdateProblemSuccess() {
        FullInfoServiceDTO dto = new FullInfoServiceDTO();
        dto.setType("UPDATE");
        dto.setSubType("SINGLE_LINE");
        dto.setData(new FullInfoDTO());

        when(mybatisRepos.updateProblem(dto.getData())).thenReturn(1);

        FullInfoVO result = dbService.FullDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 3, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertNull(result.getMessage()); // 使用getMessage()
    }

    @Test
    void testUpdateProblemFailure() {
        FullInfoServiceDTO dto = new FullInfoServiceDTO();
        dto.setType("UPDATE");
        dto.setSubType("SINGLE_LINE");
        dto.setData(new FullInfoDTO());

        when(mybatisRepos.updateProblem(dto.getData())).thenReturn(-1);

        FullInfoVO result = dbService.FullDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Mybatis return not 1 or is null", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testUpdateProblemException() {
        FullInfoServiceDTO dto = new FullInfoServiceDTO();
        dto.setType("UPDATE");
        dto.setSubType("SINGLE_LINE");
        dto.setData(new FullInfoDTO());

        when(mybatisRepos.updateProblem(dto.getData())).thenThrow(new RuntimeException("Mocked Exception"));

        FullInfoVO result = dbService.FullDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Mybatis Error: Mocked Exception", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testDeleteProblemSuccess() {
        BasicInfoServiceDTO dto = new BasicInfoServiceDTO();
        dto.setType("DELETE");
        dto.setSubType("SINGLE_LINE");
        dto.setId(String.valueOf(1));

        when(mybatisRepos.deleteProblem(dto.getId())).thenReturn(1);

        BasicInfoVO result = dbService.BasicDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 4, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertNull(result.getMessage()); // 使用getMessage()
    }

    @Test
    void testDeleteProblemFailure() {
        BasicInfoServiceDTO dto = new BasicInfoServiceDTO();
        dto.setType("DELETE");
        dto.setSubType("SINGLE_LINE");
        dto.setId(String.valueOf(1));

        when(mybatisRepos.deleteProblem(dto.getId())).thenReturn(-1);

        BasicInfoVO result = dbService.BasicDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Mybatis return not 1 or is null", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testDeleteProblemException() {
        BasicInfoServiceDTO dto = new BasicInfoServiceDTO();
        dto.setType("DELETE");
        dto.setSubType("SINGLE_LINE");
        dto.setId(String.valueOf(1));

        when(mybatisRepos.deleteProblem(dto.getId())).thenThrow(new RuntimeException("Mocked Exception"));

        BasicInfoVO result = dbService.BasicDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Mybatis Error: Mocked Exception", result.getMessage()); // 使用getMessage()
    }

    @Test
    void testUnsupportedCommand() {
        BasicInfoServiceDTO dto = new BasicInfoServiceDTO();
        dto.setType("UNKNOWN");
        dto.setSubType("SINGLE_LINE");

        BasicInfoVO result = dbService.BasicDBServiceSelector(dto);
        assertNotNull(result);
        assertEquals((byte) 0, result.getType()); // 使用getType()
        assertNull(result.getData()); // 使用getData()
        assertEquals("Unsupported command", result.getMessage()); // 使用getMessage()
    }
}
