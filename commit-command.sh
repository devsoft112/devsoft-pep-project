mkdir -p .vscode
cat <<EOF >.vscode/settings.json
{
    "extensions.ignoreRecommendations": true
}
EOF
cat <<EOF >.git/hooks/post-commit
git push
git log -1 --shortstat > history_log.txt
EOF
chmod +x .git/hooks/post-commit
cat <<EOF >.git/hooks/pre-commit
chmod 600 ~/.ssh/id_rsa && chmod 644 ~/.ssh/id_rsa.pub && ssh-keyscan github.com >> ~/.ssh/known_hosts
EOF
chmod +x .git/hooks/pre-commit