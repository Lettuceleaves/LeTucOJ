-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: letucoj
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `letucoj`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `letucoj` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `letucoj`;

--
-- Table structure for table `contest`
--

DROP TABLE IF EXISTS `contest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest` (
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cnname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `end` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `public` tinyint(1) NOT NULL,
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest`
--

LOCK TABLES `contest` WRITE;
/*!40000 ALTER TABLE `contest` DISABLE KEYS */;
INSERT INTO `contest` VALUES ('aaa','aaap','add','2025-08-01 18:20:00','2025-08-31 18:20:00',1,''),('BBB','12312','add','2025-08-03 20:06:00','2025-08-31 20:06:00',0,'');
/*!40000 ALTER TABLE `contest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contest_board`
--

DROP TABLE IF EXISTS `contest_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_board` (
  `contest_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `problem_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int NOT NULL DEFAULT '0',
  `attempts` int NOT NULL DEFAULT '0',
  `last_submit` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`contest_name`,`problem_name`,`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest_board`
--

LOCK TABLES `contest_board` WRITE;
/*!40000 ALTER TABLE `contest_board` DISABLE KEYS */;
INSERT INTO `contest_board` VALUES ('aaa','aaa','abc',100,0,'2025-08-13 12:02:04'),('aaa','aaa','test',0,1,'2025-08-13 10:55:11'),('BBB','aaa','qqqq',0,0,'2025-08-13 12:16:50');
/*!40000 ALTER TABLE `contest_board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `contest_board_view`
--

DROP TABLE IF EXISTS `contest_board_view`;
/*!50001 DROP VIEW IF EXISTS `contest_board_view`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `contest_board_view` AS SELECT 
 1 AS `contest_name`,
 1 AS `user_name`,
 1 AS `userCnname`,
 1 AS `problem_name`,
 1 AS `score`,
 1 AS `attempts`,
 1 AS `last_submit`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `contest_problem`
--

DROP TABLE IF EXISTS `contest_problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_problem` (
  `contest_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `problem_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int NOT NULL,
  PRIMARY KEY (`problem_name`,`contest_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest_problem`
--

LOCK TABLES `contest_problem` WRITE;
/*!40000 ALTER TABLE `contest_problem` DISABLE KEYS */;
INSERT INTO `contest_problem` VALUES ('aaa','abc',100),('BBB','qqqq',100),('aaa','test',100);
/*!40000 ALTER TABLE `contest_problem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contest_user`
--

DROP TABLE IF EXISTS `contest_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_user` (
  `contest_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cnname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`user_name`,`contest_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest_user`
--

LOCK TABLES `contest_user` WRITE;
/*!40000 ALTER TABLE `contest_user` DISABLE KEYS */;
INSERT INTO `contest_user` VALUES ('aaa','aaa',''),('BBB','aaa','');
/*!40000 ALTER TABLE `contest_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `problem`
--

DROP TABLE IF EXISTS `problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem` (
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cnname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `caseAmount` int NOT NULL,
  `difficulty` int NOT NULL,
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `authors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `createtime` datetime NOT NULL,
  `updateat` datetime DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `freq` float DEFAULT NULL,
  `public` tinyint(1) NOT NULL,
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `showsolution` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `problem`
--

LOCK TABLES `problem` WRITE;
/*!40000 ALTER TABLE `problem` DISABLE KEYS */;
INSERT INTO `problem` VALUES ('abc','abc',3,1,'','','2025-08-13 00:00:00',NULL,'',0,1,'#Include<stdio.h>\r\n\r\nint main() {\r\n    printf(\"111\");\r\n}',0),('P001','两数之和',10,1,'数组,哈希表','LeetCode','2023-01-01 10:00:00','2023-01-02 10:00:00','给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那两个整数，并返回他们的数组下标。',0.8,1,'使用哈希表存储数组元素及其索引，遍历数组时检查目标值减去当前元素是否在哈希表中。',1),('P002','两数相加',15,2,'链表','LeetCode','2023-01-02 10:00:00','2023-01-03 10:00:00','给出两个非空的链表，分别表示两个非负整数。它们的数字按逆序存储，每个节点包含一个数字。将这两个数相加并以相同形式返回一个表示和的链表。',0.7,1,'从链表头开始逐位相加，注意进位。',1),('P003','无重复字符的最长子串',20,2,'滑动窗口,哈希表','LeetCode','2023-01-03 10:00:00','2023-01-04 10:00:00','给定一个字符串，请你找出其中不含有重复字符的最长子串的长度。',0.6,1,'使用滑动窗口和哈希表记录字符出现的位置。',1),('P004','寻找两个正序数组的中位数',25,3,'数组,二分查找','LeetCode','2023-01-04 10:00:00','2023-01-05 10:00:00','给定两个大小分别为 m 和 n 的正序（从小到大）数组 nums1 和 nums2。请你找出并返回这两个正序数组的中位数。',0.5,1,'使用二分查找法找到中位数。',1),('P005','最长回文子串',30,2,'字符串,动态规划','LeetCode','2023-01-05 10:00:00','2023-01-06 10:00:00','给定一个字符串 s，找到 s 中最长的回文子串。',0.4,1,'使用动态规划，状态转移方程为 dp[i][j] = dp[i+1][j-1] && s[i] == s[j]。',1),('P006','Z 字形变换',35,2,'字符串','LeetCode','2023-01-06 10:00:00','2023-01-07 10:00:00','将一个给定字符串根据给定的行数，以从上往下、从左到右进行 Z 字形排列。',0.3,1,'按行模拟 Z 字形排列过程。',1),('P007','整数反转',40,1,'数学','LeetCode','2023-01-07 10:00:00','2023-01-08 10:00:00','给出一个 32 位的有符号整数，你需要将这个整数中每位上的数字进行反转。',0.2,1,'逐位反转，注意溢出。',1),('P008','字符串转换整数',45,2,'字符串','LeetCode','2023-01-08 10:00:00','2023-01-09 10:00:00','请你来实现一个 atoi 函数，使其能将字符串转换成整数。',0.1,1,'处理空格、正负号和数字部分。',1),('P009','回文数',50,1,'数学','LeetCode','2023-01-09 10:00:00','2023-01-10 10:00:00','判断一个整数是否是回文数。',0.9,1,'反转一半数字进行比较。',1),('P010','正则表达式匹配',55,3,'字符串,动态规划','LeetCode','2023-01-10 10:00:00','2023-01-11 10:00:00','给你一个字符串 s 和一个字符规律 p，请你来实现一个支持 \'.\' 和 \'*\' 的正则表达式匹配。',0.8,1,'使用动态规划，状态转移方程为 dp[i][j] = dp[i-1][j-1] || dp[i][j-2]。',1),('P011','盛最多水的容器',60,2,'数组,双指针','LeetCode','2023-01-11 10:00:00','2023-01-12 10:00:00','给定一个长度为 n 的整数数组 height。有 n 条垂线，第 i 条线的两个端点是 (i, 0) 和 (i, height[i])。找出其中的两条线，使得它们与 x 轴共同构成的容器，能够容纳最多的水。',0.7,1,'使用双指针，从两端向中间移动。',1),('P012','整数转罗马数字',65,2,'数学','LeetCode','2023-01-12 10:00:00','2023-01-13 10:00:00','将整数表示为罗马数字。',0.6,1,'按罗马数字规则逐位转换。',1),('P013','罗马数字转整数',70,1,'数学','LeetCode','2023-01-13 10:00:00','2023-01-14 10:00:00','将罗马数字表示为整数。',0.5,1,'按罗马数字规则逐位累加。',1),('P014','最长公共前缀',75,1,'字符串','LeetCode','2023-01-14 10:00:00','2023-01-15 10:00:00','编写一个函数来查找字符串数组中的最长公共前缀。',0.4,1,'横向扫描字符串数组。',1),('P015','三数之和',80,2,'数组,双指针','LeetCode','2023-01-15 10:00:00','2023-01-16 10:00:00','给定一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？',0.3,1,'排序后使用双指针。',1),('qqqq','qqqq',4,1,'qqqq','qqqq','2025-08-13 00:00:00',NULL,'',0,1,'#include <stdio.h>\r\n\r\nint main() {\r\n    int n = 0;\r\n    scanf(\"%d\", &n);\r\n    if (n == 0) {\r\n        printf(\"0\");\r\n    } else if (n == 1) {\r\n        printf(\"1\");\r\n    }  else if (n == 2) {\r\n        printf(\"2\");\r\n    }  else if (n == 3) {\r\n        printf(\"3\");\r\n    }\r\n}',NULL),('t1','数组排序和_测试',10,1,'数组,排序','LeetCode','2025-07-01 10:00:00','2025-07-05 15:00:00','给定一个整数数组 nums 和一个目标值 target，找到数组中和为 target 的两个数。',0.8,1,'使用双指针方法，时间复杂度 O(n)',1),('t2','字符串匹配_测试',15,2,'字符串,匹配','LeetCode','2025-07-02 11:00:00','2025-07-06 16:00:00','给定一个字符串 s 和一个模式 p，判断 s 是否与 p 匹配。',0.7,1,'使用动态规划，状态转移矩阵优化',1),('t3','动态规划最大子序列和_测试',12,3,'动态规划,子序列','LeetCode','2025-07-03 12:00:00','2025-07-07 17:00:00','给定一个整数数组 nums，找到具有最大和的连续子数组。',0.6,1,'使用动态规划，时间复杂度 O(n)',1),('t4','图的深度优先遍历_测试',20,2,'图,DFS','LeetCode','2025-07-04 13:00:00','2025-07-08 18:00:00','给定一个图，使用深度优先搜索（DFS）遍历图中的所有节点。',0.9,1,'使用栈来模拟递归，DFS遍历优化',1),('t5','最小生成树_测试',18,3,'图,生成树','LeetCode','2025-07-05 14:00:00','2025-07-09 19:00:00','给定一个图，求最小生成树的权值总和。',0.5,1,'使用Kruskal算法，时间复杂度 O(E log E)',1),('t6','广度优先遍历_测试',25,4,'图,BFS','LeetCode','2025-07-06 15:00:00','2025-07-10 20:00:00','给定一个图，使用广度优先搜索（BFS）遍历图中的所有节点。',0.7,1,'使用队列来模拟BFS，BFS遍历优化',1),('t7','二分查找_测试',30,1,'数组,查找','LeetCode','2025-07-07 16:00:00','2025-07-11 21:00:00','给定一个排序的数组 nums 和一个目标值 target，判断目标值是否存在于数组中。',0.8,1,'使用二分查找算法，时间复杂度 O(log n)',1),('t8','背包问题_测试',10,2,'动态规划,背包','LeetCode','2025-07-08 17:00:00','2025-07-12 22:00:00','给定一个背包容量和若干物品，求背包能装下的最大价值。',0.6,1,'使用动态规划，空间复杂度 O(n)',1),('t9','排序算法_测试',22,4,'排序,算法','LeetCode','2025-07-09 18:00:00','2025-07-13 23:00:00','给定一个整数数组 nums，排序并返回排序后的数组。',0.9,1,'使用快速排序，时间复杂度 O(n log n)',1),('test','测试',3,0,'test','test','2025-07-19 20:40:31','2025-07-19 20:40:34','test',0,1,'test',1),('vdecdzv','dvfdzbv',1,1,'','','2025-08-13 00:00:00',NULL,'',0,1,'#include <stdio.h>\r\n\r\nint main() {\r\n    printf(\"123\");\r\n}',NULL);
/*!40000 ALTER TABLE `problem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `record`
--

DROP TABLE IF EXISTS `record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `record` (
  `userName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cnname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `problemName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `result` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `timeUsed` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `memoryUsed` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `submitTime` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `record`
--

LOCK TABLES `record` WRITE;
/*!40000 ALTER TABLE `record` DISABLE KEYS */;
INSERT INTO `record` VALUES ('aaa','','test','C','#include <stdio.h>\r\n\r\nint main(void) {\r\n    int a, b, c;\r\n    scanf(\"%d%d%d\", &a, &b, &c);\r\n    int d = 0;\r\n    if (a + b + c == 8) {\r\n        d = 1;\r\n    }\r\n    printf(\"%d\", a + b + c + c); \r\n}','1 $ contest/checkAnswer: Test case 1 failed','0','0',1),('aaa','','abc','C','#Include<stdio.h>\r\n\r\nint main() {\r\n    printf(\"111\");\r\n}','5 $ practice/submit: No test cases available for this problem: abc 0','0','0',2),('aaa','','abc','C','#include<stdio.h>\r\n\r\nint main() {\r\n    printf(\"111\");\r\n}','0 $ null','0','0',3),('aaa','','abc','C','#Include<stdio.h>\r\n\r\nint main() {\r\n    printf(\"111\");\r\n}','2 $ Compilation failed: /app/localRun//userCode.c:1:2: error: invalid preprocessing directive #Include; did you mean #include?\n    1 | #Include<stdio.h>\n      |  ^~~~~~~\n      |  include\n/app/localRun//userCode.c: In function \'main\':\n/app/localRun//userCode.c:4:5: warning: implicit declaration of function \'printf\' [-Wimplicit-function-declaration]\n    4 |     printf(\"111\");\n      |     ^~~~~~\n/app/localRun//userCode.c:1:1: note: include \'<stdio.h>\' or provide a declaration of \'printf\'\n  +++ |+#include <stdio.h>\n    1 | #Include<stdio.h>\n/app/localRun//userCode.c:4:5: warning: incompatible implicit declaration of built-in function \'printf\' [-Wbuiltin-declaration-mismatch]\n    4 |     printf(\"111\");\n      |     ^~~~~~\n/app/localRun//userCode.c:4:5: note: include \'<stdio.h>\' or provide a declaration of \'printf\'','0','0',4),('aaa','','abc','C','#Include<stdio.h>\r\n\r\nint main() {\r\n    printf(\"111\");\r\n}','2 $ Compilation failed: /app/localRun//userCode.c:1:2: error: invalid preprocessing directive #Include; did you mean #include?\n    1 | #Include<stdio.h>\n      |  ^~~~~~~\n      |  include\n/app/localRun//userCode.c: In function \'main\':\n/app/localRun//userCode.c:4:5: warning: implicit declaration of function \'printf\' [-Wimplicit-function-declaration]\n    4 |     printf(\"111\");\n      |     ^~~~~~\n/app/localRun//userCode.c:1:1: note: include \'<stdio.h>\' or provide a declaration of \'printf\'\n  +++ |+#include <stdio.h>\n    1 | #Include<stdio.h>\n/app/localRun//userCode.c:4:5: warning: incompatible implicit declaration of built-in function \'printf\' [-Wbuiltin-declaration-mismatch]\n    4 |     printf(\"111\");\n      |     ^~~~~~\n/app/localRun//userCode.c:4:5: note: include \'<stdio.h>\' or provide a declaration of \'printf\'','0','0',5),('aaa','','abc','C','#include<stdio.h>\r\n\r\nint main() {\r\n    printf(\"111\");\r\n}','0 $ null','0','0',6),('aaa','','test','C','#include <stdio.h>\r\n\r\nint main() {\r\n    int n = 0;\r\n    scanf(\"%d\", &n);\r\n    printf(\"1\");\r\n    if (n == 0) {\r\n        printf(\"0\");\r\n    } else if (n == 1) {\r\n        printf(\"1\");\r\n    }  else if (n == 2) {\r\n        printf(\"0\");\r\n    }  else if (n == 3) { \r\n        printf(\"0\");\r\n    }\r\n}','1 $ practice/checkAnswer: Test case 1 failed: expected \'6\', got \'11\'','0','0',1755091529689),('www','嗡嗡嗡','test','C','#include <stdio.h>\r\n\r\nint main() {\r\n    int n = 0;\r\n    scanf(\"%d\", &n);\r\n    printf(\"1\");\r\n    if (n == 0) {\r\n        printf(\"0\");\r\n    } else if (n == 1) {\r\n        printf(\"1\");\r\n    }  else if (n == 2) {\r\n        printf(\"0\");\r\n    }  else if (n == 3) { \r\n        printf(\"0\");\r\n    }\r\n}','1 $ practice/checkAnswer: Test case 1 failed: expected \'6\', got \'11\'','0','0',1755096165703);
/*!40000 ALTER TABLE `record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sysconfig`
--

DROP TABLE IF EXISTS `sysconfig`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sysconfig` (
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `C` int NOT NULL DEFAULT '0',
  `CPP` int NOT NULL DEFAULT '0',
  `Python` int NOT NULL DEFAULT '0',
  `JS` int NOT NULL DEFAULT '0',
  `JAVA` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sysconfig`
--

LOCK TABLES `sysconfig` WRITE;
/*!40000 ALTER TABLE `sysconfig` DISABLE KEYS */;
INSERT INTO `sysconfig` VALUES ('sys',3,0,0,0,0);
/*!40000 ALTER TABLE `sysconfig` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_name` varchar(30) NOT NULL,
  `cnname` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(30) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('aaa','','$2a$10$D84cgD5p74uC8bzpr50stO3Nyl5CnsFcnb/wXG45XkZ7uPSEubPym','ROOT',1),('aaaaa','','AAAaaa1.','USER',0),('aaab','','aaa','USER',1),('www','嗡嗡嗡','$2a$10$8ESKoXRWZfb9GWZfO.N0YOrW7MS5p07X0axhItTm.o0mj60WI0W4G','USER',1);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `letucoj`
--

USE `letucoj`;

--
-- Final view structure for view `contest_board_view`
--

/*!50001 DROP VIEW IF EXISTS `contest_board_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `contest_board_view` AS select `u`.`contest_name` AS `contest_name`,`u`.`user_name` AS `user_name`,`u`.`cnname` AS `userCnname`,`p`.`problem_name` AS `problem_name`,coalesce(`b`.`score`,0) AS `score`,coalesce(`b`.`attempts`,0) AS `attempts`,`b`.`last_submit` AS `last_submit` from ((`contest_user` `u` join `contest_problem` `p`) left join `contest_board` `b` on(((`b`.`contest_name` = `u`.`contest_name`) and (`b`.`user_name` = `u`.`user_name`) and (`b`.`problem_name` = `p`.`problem_name`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-14  0:55:47
