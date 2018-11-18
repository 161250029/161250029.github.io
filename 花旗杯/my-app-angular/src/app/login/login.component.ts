import { Component, OnInit, Input } from '@angular/core';
import { LoginService } from './services/login.service';
import { Router } from '@angular/router';
@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    @Input() username: string = '';
    @Input() password: string = '';
    @Input() remember: boolean = false;
    private isLoginBtnDisable: boolean = true;
    private usernameErrorTxt: string = '';
    private passwordErrorTxt: string = '';
    private maskShow: boolean = false;
    private loginErrorTxt: string;
    
    constructor(private loginService: LoginService,
        private router: Router) { }

    ngOnInit() {
        // this.checkLogined()
        this.checkRemembered();
    }

    checkLogined() {
        if (localStorage.apiAccessToken && localStorage.apiAccessToken != '') {
            this.router.navigate(['/account-summary']);
        }
    }

    checkRemembered() {
        if (localStorage.apiRemeberUsername) {
            this.remember = localStorage.apiRemeberUsername;
            this.username = localStorage.apiRemeberedUsername;
        }
    }

    checkLoginBtnDisable(): boolean {
        if (this.username != '' && this.password != '') {
            return false;
        } else {
            return true;
        }
    }

    checkUsername(): boolean {
        return true;
    }

    checkPassword(): boolean {
        return true;
    }

    login(): void {
        if (this.checkLoginBtnDisable()) {
            return;
        };
        this.maskShow = true;
        this.loginService.loginStep1()
            .then((res) => {
                return Promise.resolve(res.access_token);
            })
            .then((res) => {
                this.loginService.loginStep2(res).subscribe(resp => {
                    // display its headers
                    this.loginService.loginStep3(resp.headers.get('bizToken'), 
                    this.username,this.password,resp.headers.get('eventid'),resp.body.modulus,resp.body.exponent).then((res) => {
                        localStorage.apiAccessToken = res.access_token;
                        if (this.remember) {
                            localStorage.apiRemeberUsername = true;
                            localStorage.apiRemeberedUsername = this.username;
                        } else {
                            localStorage.removeItem('apiRemeberUsername');
                            localStorage.removeItem('apiRemeberedUsername');
                        }
                        this.maskShow = false;
                        this.router.navigate(['/account-summary']);
                    }).catch(error => {
                        this.maskShow = false;
                        this.loginErrorTxt = "Invalid Username or Password";
                    });
                });
            })
    }
}
