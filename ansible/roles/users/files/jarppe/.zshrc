#
# .zshrc
#

autoload -U compinit
compinit

bindkey -v

typeset -ga preexec_functions
typeset -ga precmd_functions
typeset -ga chpwd_functions

setopt pushdsilent
setopt pushdtohome
setopt prompt_subst

if [[ -z "$SSH_CONNECTION" ]]; then
  MN=""
else
  MN="[$(hostname)]"
fi

autoload -U colors && colors
PS1="%{$fg_bold[white]%}${MN}%~> %{$reset_color%}"

alias l='ls -F'
alias ll='ls -Fl'
alias lll='ls -Fla'
alias m=less
alias cd=pushd
