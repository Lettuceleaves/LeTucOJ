//import com.LetucOJ.practice.model.CodeDTO;
//import com.LetucOJ.practice.service.PracticeService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = com.LetucOJ.practice.PracticeApplication.class)
//public class PracticeServiceTest {
//
//    @Autowired
//    private PracticeService practiceService;
//
//    @Test
//    public void testRunService() throws Exception {
//        String filePath = "E:\\projects\\LetucOJ\\test\\testCode\\test" + 1 + ".txt";
//        String userCode = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
//        CodeDTO codeMessage = new CodeDTO();
//        codeMessage.setCode(userCode);
//        codeMessage.setService((byte) 0);
//        codeMessage.setProblemId("test");
//        System.out.println(practiceService.submitTest(codeMessage));
//    }
//}