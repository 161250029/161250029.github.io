# 如何用nvm安装node和npm

## 安装nvm
1. 安装nvm
    `git clone https://github.com/creationix/nvm.git ~/.nvm && cd ~/.nvm &&   git checkout git describe --abbrev=0 --tags`

2. 配置环境变量

    > 编辑 .bashrc, 加入 source ~/.nvm/nvm.sh ，保存，然后运行 source  .bashrc

3. 用nvm --version 查看版本

## 安装node

1. 查看可用的node版本

   `nvm ls-remote`

2. 选择版本并安装
   ` nvm install 8.11.3 `
3. 设置为默认版本
   `nvm alias default 8.11.3`

