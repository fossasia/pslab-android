module.exports = async ({github, context, core}) => {
  const title_line = '#<!-- Add issue number here. This will automatically closes the issue. If you do not solve the issue entirely, please change the message to e.g. "First steps for issues #IssueNumber" -->';
  const change_line = "- <!-- Add here what changes were made in this pull request and if possible provide links. -->";
  const screen_line = '<!-- Add screen shots/screen recordings of the layout where you made changes or a `*.gif` containing a demonstration. Fill "> N/A" if the change is not a UI fix. -->';
  const check_boxes = '- [ ] ';

  const pullrequest = await github.rest.pulls.get({
    owner: context.repo.owner,
    repo: context.repo.repo,
    pull_number: context.issue.number,
  });

  const pr_body = pullrequest.data.body;
  const pr_author = pullrequest.data.user.login;
  const pr_reviewers = ['CloudyPadmal'];
  const pr_comment = "Hello @" + pr_author + ", ";
  const pr_exclude = ['dependabot[bot]'];
  
  if (!pr_author.includes(pr_exclude)) {
    return;
  }
  
  await github.rest.issues.addAssignees({
    issue_number: context.issue.number,
    owner: context.repo.owner,
    repo: context.repo.repo,
    assignees: [pr_author]
  });
  
  if (pr_body.includes(title_line) || pr_body.includes(change_line) || pr_body.includes(screen_line) || pr_body.includes(check_boxes)) {
    await github.rest.issues.createComment({
      issue_number: context.issue.number,
      owner: context.repo.owner,
      repo: context.repo.repo,
      body: pr_comment + "Thank you for this pull request. However, it looks like the pull request body is incomplete. Make sure you have added all the missing details.\n - Issue number you're fixing\n - Changes made\n - Screenshots or recordings if any\n - Tick the check boxes if the points are made."
    });
    return;
  }

  if (!pr_author.includes(pr_reviewers)) {
    await github.rest.issues.addLabels({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: context.issue.number,
      labels: [
        'Status: Review Required'
      ]
    });
    await github.rest.pulls.requestReviewers({
      pull_number: context.issue.number,
      owner: context.repo.owner,
      repo: context.repo.repo,
      reviewers: pr_reviewers
    });
  }

  const pr_event = context.payload.action;
  await github.rest.issues.createComment({
    issue_number: context.issue.number,
    owner: context.repo.owner,
    repo: context.repo.repo,
    body: pr_comment + (pr_event == 'edited' ? "Thank you for the update. Maintainers will look into this as soon as possible." : "Thank you for this pull request. Maintainers will review it as soon as possible.")
  });
}
