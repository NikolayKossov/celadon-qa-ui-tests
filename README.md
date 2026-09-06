# Celadon Junior QA — UI automation portfolio

Проект автоматизации на Java, Selenide, JUnit 5 и Allure для вакансии,
размещённой в Google Apps Script.

## Запуск

Нужны JDK 17+ (проверка выполнялась с JDK 21), Chrome и доступ к интернету.
Gradle Wrapper загружает Gradle и зависимости, Selenium Manager подбирает драйвер.

```powershell
.\gradlew.bat test
.\gradlew.bat celadon_test
.\gradlew.bat celadon_test '-Dheadless=true'
.\gradlew.bat celadon_test '-Dbrowser_size=390x844' '-Dheadless=true'
.\gradlew.bat allureReport
```

Удалённый запуск:

```powershell
.\gradlew.bat celadon_test '-Dremote_url=http://localhost:4444/wd/hub' '-Dbrowser=chrome'
```

URL можно переопределить через `-Dceladon.url=...`, версию браузера —
через `-Dbrowser_version=...`. Для повторного выполнения без изменения файлов
добавьте `--rerun-tasks`. Задачи `test` и `celadon_test` запускают тесты Celadon.


## Покрытие: 11 сценариев

| Проверка | Ожидаемый результат |
| --- | --- |
| Вакансия | Junior QA, Poland, Remote · Full-time, B1+ |
| Требования | GUI/UI/UX, API, архитектура, английский, дополнительный опыт |
| Условия | Все шесть заявленных преимуществ видимы |
| Отклик | Правильные email, тема mailto и инструкция на странице |
| Ссылки, 7 параметров | Cases, About, Website, LinkedIn, Privacy Policy, Telegram, email |

## Что демонстрирует проект

- Page Object с fluent API и отдельной конфигурацией запуска.
- Корректное переключение через два вложенных iframe.
- Ожидания Selenide вместо фиксированных задержек.
- Параметризованные проверки ссылок и отдельные бизнес-сценарии.
- Изолированная браузерная сессия на каждый тест и гарантированное закрытие.
- Allure: шаги, severity, скриншот и HTML приложения после теста.

Тесты проверяют адреса ссылок, а не доступность внешних сайтов. Почтовый клиент
не открывается, письма и резюме не отправляются. Здесь нет формы, поэтому сценарии
заполнения формы к этой странице неприменимы.
Скриншоты — диагностические вложения, а не автоматическое сравнение с эталоном.
Мобильный запуск проверяет те же сценарии в узком окне, не заменяя полноценный
аудит адаптивности. Видео и Telegram-уведомления не настроены.

## Docker

Добавлены два контейнера: JDK 21 с Gradle Wrapper и Selenium Standalone Chrome.
Compose ждёт готовности браузера перед запуском тестов. Порты браузера наружу
не публикуются. Нужен Docker с Compose v2 и Linux containers (на Windows — Docker Desktop).
Образ Chrome рассчитан на x86-64. Java и Chrome на хосте для этого запуска не нужны.

```powershell
New-Item -ItemType Directory -Force build | Out-Null
docker compose up --build --abort-on-container-exit --exit-code-from tests
docker compose down --remove-orphans
```

На Linux перед запуском создайте каталог результатов от имени текущего пользователя:

```bash
mkdir -p build
export LOCAL_UID=$(id -u) LOCAL_GID=$(id -g)
docker compose up --build --abort-on-container-exit --exit-code-from tests
docker compose down --remove-orphans
```

Код завершения `up` отражает результат тестов. Перед каждым прогоном очищаются
сгенерированные результаты и отчёты в `build`, чтобы не смешивать запуски.
Новый Allure HTML сохраняется в `build/reports/allure-report/allureReport`,
сырые результаты — в `build/allure-results`. Для просмотра HTML без локальной Java:

```powershell
docker run --rm -p 127.0.0.1:8088:80 --mount "type=bind,source=$((Get-Location).Path)/build/reports/allure-report/allureReport,target=/usr/share/nginx/html,readonly" nginx:stable-alpine
```

Откройте http://localhost:8088. Остановка сервера — Ctrl+C.

## Jenkins / облачный сервер

В репозитории подготовлен `Jenkinsfile`. Нужен Linux-агент Jenkins с меткой
`docker`, Docker Engine и Compose v2. Пользователь агента должен иметь доступ
к Docker daemon; используйте выделенный агент для доверенного кода проекта.
Pipeline выполняет checkout, сборку контейнера, тесты, публикацию результатов
и очистку контейнеров. Рабочая папка job очищается перед checkout.

1. Загрузите проект в свой Git-репозиторий.
2. В Jenkins установите плагины Pipeline, Git, JUnit и Allure.
3. В Tools настройте Allure Commandline; для его запуска на агенте нужна Java.
4. Создайте Pipeline job → Pipeline script from SCM → Git, укажите репозиторий
   и путь `Jenkinsfile`. При необходимости добавьте credentials в Jenkins.
5. Нажмите Build Now. Результаты будут доступны в Test Result, Artifacts и Allure.

Для автоматического запуска настройте webhook Git-провайдера и соответствующий
trigger в Jenkins. Облачный сервер, репозиторий и webhook этим изменением не создаются.
Контейнерный запуск и Jenkins пока не проверены: Docker отсутствует в текущем окружении.

Документация: [Selenium Docker](https://github.com/SeleniumHQ/docker-selenium),
[Allure в Jenkins](https://www.jenkins.io/doc/pipeline/steps/allure-jenkins-plugin/).

## Демонстрация на собеседовании

Проверено 6 сентября 2026: JDK 21, Chrome 153, headless, 1440×1000 —
**11 из 11 тестов прошли**, `clean test allureReport`: BUILD SUCCESSFUL.
Allure-отчёт: `build/reports/allure-report/allureReport/index.html`.
Мобильный и удалённый запуски не выполнялись.

1. Покажите `CeladonVacancyPage` и объясните вложенные iframe.
2. Запустите `celadon_test` в видимом браузере.
3. Откройте Allure и покажите шаги и диагностические вложения.
4. Объясните ограничения покрытия и как добавили бы проверки формы/API,
   если бы приложение предоставляло соответствующие функции.

# celadon-qa-ui-tests
