//import com.LetucOJ.run.model.vo.ResultVO;
//import com.LetucOJ.run.service.impl.RunServiceImpl;
//import com.LetucOJ.run.service.RunService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Stream;
//
//@Slf4j
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = RunServiceImpl.class)
//public class RunServiceTest {
//
//    @Autowired
//    private RunService runService;
//
//    @ParameterizedTest
//    @MethodSource("provideIntegers")
//    void testCode(int i) throws IOException {
//
//
//        System.out.println("--------------" + i + "----------------");
//        String filePath = "E:\\projects\\OMS2.0\\test\\testCode\\test" + i + ".txt";
//        String userCode = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
//
//        List<String> inputFiles = new ArrayList<>();
//        inputFiles.add(userCode);
//        String test1 = "1 2 3";
//        String test2 = "4 5 6";
//        String test3 = "7 8 9";
//        inputFiles.add(test1);
//        inputFiles.add(test2);
//        inputFiles.add(test3);
//
//        // 调用方法
//        ResultVO resultVO = null;
//        try {
//            resultVO = runService.run(inputFiles, true);
//        } catch (Exception e) {
//            log.message("e: ", e);
//        }
//
//        String testFilePath = "E:\\projects\\OMS2.0\\test\\testSpace";
//        try {
//            Files.walk(Paths.get(testFilePath))
//                    .map(Path::toFile)
//                    .forEach(File::delete);
//        } catch (IOException e) {
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!");
//            return;
//        }
//
//        if (resultVO == null) {
//            System.out.println("ans is null");
//            return;
//        }
//        System.out.println(resultVO);
//    }
//
//    public static Stream<Integer> provideIntegers() throws IOException {
//
//        // 获取testCode文件夹下的文件数
//        int fileCount = (int) java.nio.file.Files.list(java.nio.file.Paths.get("E:\\projects\\OMS2.0\\test\\testCode")).count();
//        List<Integer> numbers = new ArrayList<>();
//        for (int i = 1; i <= fileCount; i++) {
//            numbers.add(i);
//        }
//        return numbers.stream();
//
//    }
//}
