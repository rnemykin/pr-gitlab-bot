# pr-gitlab-bot
Bot for automatic process gitlab pull requests.
Bot sends message in provided `app.telegram.chatId` about pull request, update it information with comments and approves count.
After merge bot will delete pull request message from chat

# Configuration
 - app.gitlab.pr.userNames - usernames in gitLab, which PR's bot will check
 - app.job.notifyAboutMergedPr - cron expression for check and delete messages about merged PR
 - app.job.notifyAboutOpenedPr - cron expression for check and notice about opened PR
 - app.telegram.token - bot token
 - app.telegram.botName - bot name
 - app.telegram.proxyHost - telegram proxy host
 - app.telegram.proxyPort - telegram proxy port
 - app.telegram.proxyPassword - telegram proxy pass
 - app.telegram.proxyUser - telegram proxy username
 - app.telegram.chatId - telegram chat id for send notification about PR
 
## Configuration example
```yml
app:
  gitlab:
    url: https://gitlab.rnemykin.ru
    token: secret_token
    pr:
      userNames:
        - rnemykin
  job:
    notifyAboutMergedPr: 0 * 7-19 * * MON-FRI
    notifyAboutOpenedPr: '0 */2 7-19 * * MON-FRI'
```

