# Бодит Maven гаралтууд

Энд `mvn test`-ийн бодит гаралтыг хадгална. ZIP-д **зохиомол PASS/FAIL гаралт оруулаагүй**.

Debian дээр `bash scripts/run-validation.sh` ажиллуулсны дараа:

- `mvn-test.txt` — эх сурвалж сэргэсэн, 10 tests PASS
- `mvn-test-mutant-1.txt` — inactive student check-ийг түр устгасан: BUILD FAILURE
- `mvn-test-mutant-2.txt` — saveRegistration-ийг түр устгасан: BUILD FAILURE
- `mvn-test-mutant-3.txt` — courseId null шалгалтыг түр устгасан: BUILD FAILURE
- `validation-summary.txt` — бодит exit code-уудын тайлан

Эдгээрийг GitHub-д commit хийхийн өмнө бүгдийг шалгана.
