import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { UUID } from 'angular2-uuid';
import { Router } from '@angular/router';

const combine = "f2bdc852-3396-4b87-9e2e-4ef3ff83a983:dH2eR3qC8gC5uX7mE7cQ4dR5vU3wC8iD7nN1uI2uS6qJ1cX8xF"
const encodedCombine = window.btoa(decodeURIComponent(encodeURIComponent(combine)));

const httpOptions = {
    headers: new HttpHeaders({
        'content-type': 'application/x-www-form-urlencoded',
        authorization: "Basic " + encodedCombine,
        accept: 'application/json'
    })
};

@Injectable()
export class AccountSummaryService {
    private accountSummaryUrl = 'https://sandbox.apihub.citi.com/gcb/api/v1/accounts'; 
    constructor(private http: HttpClient, private router: Router) { }

    getAccountSummary(accessToken): Promise<any> {
        let uuid = UUID.UUID();
        let httpOptions = {
            headers: new HttpHeaders({
                'content-type': 'application/json',
                authorization: "Bearer " + accessToken,
                accept: 'application/json',
                client_id: "f2bdc852-3396-4b87-9e2e-4ef3ff83a983",
                uuid: uuid
            })
        };
        return this.http.get(this.accountSummaryUrl, httpOptions)
            .toPromise()
            .then(response => response)
            .catch(this.handleError.bind(this));
    }

    getAccountDetails(accountId, accessToken): Promise<any> {
        let uuid = UUID.UUID();
        let httpOptions = {
            headers: new HttpHeaders({
                'content-type': 'application/json',
                authorization: "Bearer " + accessToken,
                accept: 'application/json',
                client_id: "f2bdc852-3396-4b87-9e2e-4ef3ff83a983",
                uuid: uuid
            })
        };
        return this.http.get(this.accountSummaryUrl + '/' + accountId, httpOptions)
            .toPromise()
            .then(response => response)
            .catch(this.handleError.bind(this));
    }

    private handleError(error: any): Promise<any> {
        if (error.status == 401) {
            localStorage.removeItem('apiAccessToken'); 
            this.router.navigate(['/login']);
        }
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }
}
