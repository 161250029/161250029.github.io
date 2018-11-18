import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { AccountSummaryService } from './services/account-summary.service';
import { LoginService } from '../login/services/login.service';

@Component({
    selector: 'app-login',
    templateUrl: './account-summary.component.html',
    styleUrls: ['./account-summary.component.css']
})
export class AccountSummaryComponent implements OnInit {
    private accessToken:string;
    private savings: object = {};
    private insurance: object = {};
    private accountsArr: any[];
    private insuranceArr: any[];
    private currentTransactionTab: number = 0;
    private accountDetails: object = {};
    private isOpenAccountDetails: boolean = false;
    private maskShow = true;

    constructor(private accountSummaryService: AccountSummaryService,
        private loginService: LoginService,
        private router: Router ) { }

    ngOnInit() {
        this.accessToken = localStorage.apiAccessToken;
        this.getAccountSummary();
        
        console.log("service token" + this.loginService.accessToken);
    }

    getAccountSummary() {
        this.accountSummaryService.getAccountSummary(this.accessToken).then((res) => {
            this.maskShow = false;
            this.savings = res.accountGroupSummary[0];
            this.accountsArr = this.savings["accounts"];
            this.insurance = res.accountGroupSummary[1];
            this.insuranceArr = this.insurance["insurancePolicies"];
        })
    }

    openAccountDetails(accountId) {
        this.getAccountDetails(accountId);
    }

    getAccountDetails(accountId) {
        this.maskShow = true;
        this.accountSummaryService.getAccountDetails(accountId, this.accessToken).then((res) => {
            this.accountDetails = res.savingsAccount;
            this.isOpenAccountDetails = true;
            this.maskShow = false;
        })
    }

    //functions on details panel
    switchTrasactionTab(index) {
        if (this.currentTransactionTab === index) {
            return;
        }
        this.currentTransactionTab = index;
    }

    backToSummary() {
        this.isOpenAccountDetails = false;
    }
}
