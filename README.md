# Лаборатори №8 — Mock объектууд / Mockito

[![Java CI](https://github.com/anrzeoui/lab08-mockito/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/anrzeoui/lab08-mockito/actions/workflows/test.yml)

**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест  
**Оюутан:** Ө. Анар  
**Оюутны код:** B242270129  
**Тестийн бай:** `CourseRegistrationService.register(studentId, courseId)`  
**Эх сурвалж:** Багшийн өгсөн `starter.zip`; `pom.xml`, `src/main` файлуудыг өөрчлөөгүй.

## 1. Орчин

Багшийн шаардлага: JDK 17+, Maven, JUnit Jupiter 5.10.2, Mockito 5.11.0, Surefire 3.2.5. Төслийн compiler target нь Java 17; Debian-д `default-jdk` ашиглан орчныг бэлдсэн.

**Debian компьютер дээрх хувилбарууд:**

<!-- ENVIRONMENT_START -->
**`java -version`:**

```text
openjdk version "21.0.12.1" 2026-08-18
OpenJDK Runtime Environment (build 21.0.12.1+1-1-deb13u1-Debian)
OpenJDK 64-Bit Server VM (build 21.0.12.1+1-1-deb13u1-Debian, mixed mode, sharing)
```

**`mvn -version`:**

```text
[1mApache Maven 3.9.9[m
Maven home: /usr/share/maven
Java version: 21.0.12.1, vendor: Debian, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "6.12.113+deb13-amd64", arch: "amd64", family: "unix"
```
<!-- ENVIRONMENT_END -->

## 2. Бүтэц

```text
lab08-mockito/
├── pom.xml
├── .gitignore
├── README.md
├── src/main/java/mn/edu/must/sqat/
│   ├── Student.java
│   ├── StudentRepository.java
│   ├── EmailSender.java
│   └── CourseRegistrationService.java
├── src/test/java/mn/edu/must/sqat/
│   └── CourseRegistrationServiceTest.java
├── scripts/run-validation.sh
└── results/
    ├── mvn-test.txt                 
    ├── mvn-test-mutant-1.txt        
    ├── mvn-test-mutant-2.txt        
    ├── mvn-test-mutant-3.txt        
    └── validation-summary.txt      
```

## 3. Хэсэг A — A1–A8 тестүүд

| Тест | Шалгах нөхцөл | Mockito/JUnit хэрэгсэл |
|---|---|---|
| A1 | Active student, prerequisite хангагдсан → OK; бүртгэл хадгална, и-мэйл илгээнэ | `when`, `verify` |
| A2 | Оюутан байхгүй → `ERROR_NO_STUDENT`; хадгалалт, имэйл байхгүй | `never()` |
| A3 | Идэвхгүй оюутан → `ERROR_INACTIVE_STUDENT`; email хамаарал огт дуудагдахгүй | `verifyNoInteractions` |
| A4 | Prerequisite дутуу → `ERROR_PREREQUISITES`; хадгалалт хийхгүй | `when`, `never()` |
| A5 | EmailSender exception → `OK`; бүртгэл хэвээр хадгалагдана | `doThrow` |
| A6 | `studentId=null` болон `courseId=null` тус бүр `IllegalArgumentException`; dependency огт дуудахгүй | `assertThrows` |
| A7 | Илгээгдсэн email body хичээлийн кодтой | `ArgumentCaptor` |
| A8 | MISSING/INACTIVE/NO_COURSE 3 нөхцөл, тус бүр зөв алдаа | `@ParameterizedTest`, `@CsvSource` |

**Бодитоор ажилласан тестийн тоо:** 7 энгийн тест + A8-ийн 3 мөр = **10 test executions**. `results/mvn-test.txt`-д **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0** болон **BUILD SUCCESS** гарсан. Мутацийн туршилтын дараа сэргээсэн эх код дээр дахин шалгасан.

**A8 дахь `lenient()` тухай:** Mockito-ийн `MockitoExtension` strict stubs ашигладаг тул дуудаагүй `when` stub үлдвэл `UnnecessaryStubbingException` гардаг. Энэ төсөлд A8-ийн scenario бүрт зөвхөн тухайн замд ашиглагдах stub-уудыг `switch` дотор үүсгэсэн тул `lenient()` хэрэглэх шаардлагагүй; шаардлагагүй stub-ийг lenient болгохоос илүү энэ нь алдааг хурдан илрүүлнэ.

## 4. Хэсэг B — 3 мутацийн бодит үр дүн

Тестерийн Debian компьютер дээр `bash scripts/run-validation.sh` ажиллуулсан. Энэ скрипт мутаци бүрийг **түр** оруулж, `mvn test`-ийн бүрэн гаралтыг өөр файлд хадгалан, эцэст нь `CourseRegistrationService.java`-ийг сэргээдэг. Доорх үр дүнг тестерийн илгээсэн **Maven терминалын бодит гаралтаас** нэгтгэлээ.

| Мутаци | Өөрчилсөн дүрэм | Унасан тестийн нэр | Failures / Errors | Maven үр дүн | Нотолгоо |
|---|---|---|---|---|---|
| **Mutant 1** | `if (!student.active())` шалгалтыг түр устгасан | `register_inactiveStudent_returnsErrorWithoutEmailInteraction` (**A3**); `register_invalidScenario_returnsExpectedErrorWithoutSaving(...)[2]` (**A8 INACTIVE**) | **2 / 0** | **BUILD FAILURE** | [`mvn-test-mutant-1.txt`](results/mvn-test-mutant-1.txt) |
| **Mutant 2** | `repository.saveRegistration(...)` дуудалтыг түр устгасан | `register_activeStudentWithPrerequisites_savesAndEmails` (**A1**); `register_emailSenderThrows_stillReturnsOkAndSavesRegistration` (**A5**) | **2 / 0** | **BUILD FAILURE** | [`mvn-test-mutant-2.txt`](results/mvn-test-mutant-2.txt) |
| **Mutant 3** | `courseId == null` хамгаалалтыг түр устгасан | `register_nullStudentIdOrCourseId_throwsWithoutDependencyCalls` (**A6**) | **1 / 0** | **BUILD FAILURE** | [`mvn-test-mutant-3.txt`](results/mvn-test-mutant-3.txt) |

**Эцсийн шалгалт** — [`results/mvn-test.txt`](results/mvn-test.txt):

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

`results/validation-summary.txt`-д эхний baseline `exit=0`, Mutant 1–3 тус бүр `exit=1`, эцсийн сэргээсэн baseline `exit=0` гарсан. Нэмэлтээр `git diff -- src/main/java/mn/edu/must/sqat/CourseRegistrationService.java` командын гаралт хоосон байсан. Иймээс 3 мутант тус бүрийг тестүүд илрүүлж, эцэст нь эх код сэргээгдэн бүх **10 тест PASS** болсон.

**Тайлбар:** Mutant 1-ийн A8 INACTIVE тохиолдол нь параметржүүлсэн тестийн хоёр дахь мөр. Mutant 3-д A6 тест `studentId == null` ба `courseId == null` гэсэн **хоёр тусдаа оролтыг** шалгадаг учраас `courseId == null` хамгаалалтыг устгах алдааг илрүүлсэн. `Failures=2,2,1` гэсэн тоонууд нь тус бүрийн Maven log-той нийцнэ.

## 5. Mock ба Fake, verify-ийн хэрэглээ — дүгнэлт

Mockito mock нь хамаарлын хариулт, exception болон дуудлагын тоог тест бүр дээр тодорхой удирдах боломж олгосон. Гараар бичсэн fake нь интерфейсийн ажиллах энгийн хэрэгжүүлэлт учраас олон тест дээр дахин ашиглаж болох ч нэмэлт логик, төлөв хадгалах код шаарддаг. Энэ лабад өгөгдлийн сан болон SMTP үйлчилгээний интерфейсийг mock хийснээр бизнес логикийг гадаад системээс тусгаарласан. Харин зөвхөн өгөгдөл дамжуулдаг `Student` record шиг энгийн объект болон тестлэгдэх `CourseRegistrationService`-ийг mock хийх шаардлагагүй. Stub-аа зөв сонгох нь и-мэйл сервер ажиллаагүй үед ч бүртгэл хадгалагдах бизнесийн дүрмийг шалгахад тусалсан. `verify`-г хэт олон дотоод дуудлага дээр хэрэглэвэл кодын хэрэгжүүлэлт бага зэрэг өөрчлөгдөхөд зан төлөв зөв хэвээр байсан ч тест эвдрэх эрсдэлтэй. Иймээс хэрэглэгчид харагдах үр дүнг `assertEquals`-оор, чухал side effect-үүдийг `verify`-аар шалгах нь илүү оновчтой.


## 6. GitHub Actions CI

GitHub Actions-ийн `.github/workflows/test.yml` нь `push`, `pull_request` болон гараар эхлүүлэх (`workflow_dispatch`) үед Ubuntu дээр Temurin JDK 21 суулгаж `mvn test` ажиллуулна. Тестийн үр дүнг workflow-ийн **Actions** хуудаснаас харж болно. Тестийн тайланг `surefire-reports` artifact болгон хадгална. Дээрх badge нь `main` салбарын **бодит** CI төлөвийг харуулна (анхны push хийхээс өмнө ногоон болохгүй).
