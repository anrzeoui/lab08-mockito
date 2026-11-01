# Лаборатори №8 — Mock объектууд / Mockito

**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест  
**Оюутан:** Ө. Анар  
**Оюутны код:** B242270129  
**Тестийн бай:** `CourseRegistrationService.register(studentId, courseId)`  
**Эх сурвалж:** Багшийн өгсөн `starter.zip`; `pom.xml`, `src/main` файлуудыг өөрчлөөгүй.

## 1. Орчин

Багшийн заавар: JDK 17+, Maven, JUnit Jupiter 5.10.2, Mockito 5.11.0, Surefire 3.2.5. Төслийн compiler target нь Java 17 тул Debian-ийн OpenJDK 21 дээр ажиллах боломжтой.

**Доорх хоёр командыг өөрийн Debian дээр ажиллуулаад БОДИТ гаралтыг энд хуул:**

```bash
java -version
mvn -version
```

> Java болон Maven-ийн яг хувилбарыг хэрэглэгчийн компьютер дээрээс баталгаажуулах ёстой; ZIP-д таамгаар бичээгүй.

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
    ├── mvn-test.txt                 (ажиллуулсны дараа)
    ├── mvn-test-mutant-1.txt        (ажиллуулсны дараа)
    ├── mvn-test-mutant-2.txt        (ажиллуулсны дараа)
    ├── mvn-test-mutant-3.txt        (ажиллуулсны дараа)
    └── validation-summary.txt      (ажиллуулсны дараа)
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

**Хүлээгдэж буй нийт тоо:** 7 энгийн тест + A8-ийн 3 мөр = **10 test executions**. Багшийн үнэлгээнд орох бодит тоог `results/mvn-test.txt`-ийн төгсгөлөөс авна.

**A8 дахь `lenient()` тухай:** Mockito-ийн `MockitoExtension` strict stubs ашигладаг тул дуудаагүй `when` stub үлдвэл `UnnecessaryStubbingException` гардаг. Энэ төсөлд A8-ийн scenario бүрт зөвхөн тухайн замд ашиглагдах stub-уудыг `switch` дотор үүсгэсэн тул `lenient()` хэрэглэх шаардлагагүй; шаардлагагүй stub-ийг lenient болгохоос илүү энэ нь алдааг хурдан илрүүлнэ.

## 4. Хэсэг B — 3 мутаци

Дараах 3 мутацийг тус бүрээр түр оруулаад `mvn test` ажиллуулж, дараа нь starter-ийн кодыг заавал сэргээнэ:

| Мутаци | Эвдсэн логик | Унах ёстой тестүүд (таамаг; бодит log-оор батална) | Нотолгоо |
|---|---|---|---|
| Mutant 1 | `!student.active()` шалгалтыг устгах | A3, A8-ийн INACTIVE мөр | `results/mvn-test-mutant-1.txt` |
| Mutant 2 | `saveRegistration(...)` дуудалтыг устгах | A1, A5 | `results/mvn-test-mutant-2.txt` |
| Mutant 3 | `courseId == null` хамгаалалтыг устгах | A6 (`courseId=null`) | `results/mvn-test-mutant-3.txt` |

Ашиглах команд:

```bash
mvn test
bash scripts/run-validation.sh
```

**Дууссаны дараа:** `results/mvn-test.txt` дотор `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` болон `BUILD SUCCESS`; мутацийн гурван log тус бүрд `BUILD FAILURE` ба Failures/Errors ≥ 1 байх шаардлагатай. Эдгээр нь хүлээгдэх үр дүн, бэлэн гэж батлаагүй. Бодит файлуудыг ажиллуулж үүсгэсний дараа энэ хэсгийн хүснэгт дэх унасан тестийн нэрсийг log-той тулгаж баталгаажуулна.

## 5. Mock ба Fake, verify-ийн хэрэглээ — дүгнэлт

Mockito mock нь хамаарлын хариулт, exception болон дуудлагын тоог тест бүр дээр тодорхой удирдах боломж олгосон. Гараар бичсэн fake нь интерфейсийн ажиллах энгийн хэрэгжүүлэлт учраас олон тест дээр дахин ашиглаж болох ч нэмэлт логик, төлөв хадгалах код шаарддаг. Энэ лабад өгөгдлийн сан болон SMTP үйлчилгээний интерфейсийг mock хийснээр бизнес логикийг гадаад системээс тусгаарласан. Харин зөвхөн өгөгдөл дамжуулдаг `Student` record шиг энгийн объект болон тестлэгдэх `CourseRegistrationService`-ийг mock хийх шаардлагагүй. Stub-аа зөв сонгох нь и-мэйл сервер ажиллаагүй үед ч бүртгэл хадгалагдах бизнесийн дүрмийг шалгахад тусалсан. `verify`-г хэт олон дотоод дуудлага дээр хэрэглэвэл кодын хэрэгжүүлэлт бага зэрэг өөрчлөгдөхөд зан төлөв зөв хэвээр байсан ч тест эвдрэх эрсдэлтэй. Иймээс хэрэглэгчид харагдах үр дүнг `assertEquals`-оор, чухал side effect-үүдийг `verify`-аар шалгах нь илүү оновчтой.
