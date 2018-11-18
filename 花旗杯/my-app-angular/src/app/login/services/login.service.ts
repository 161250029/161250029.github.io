import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { UUID } from 'angular2-uuid';

import { Observable } from 'rxjs/Observable';
const clientID = "f2bdc852-3396-4b87-9e2e-4ef3ff83a983";
const clientSecret = "dH2eR3qC8gC5uX7mE7cQ4dR5vU3wC8iD7nN1uI2uS6qJ1cX8xF";
const combinePlainTxt = clientID + ":" + clientSecret;
const encryptedKey = window.btoa(decodeURIComponent(encodeURIComponent(combinePlainTxt)));

const httpOptions = {
    headers: new HttpHeaders({
        'content-type': 'application/x-www-form-urlencoded',
        authorization: "Basic " + encryptedKey,
        accept: 'application/json'
    })
};

@Injectable()
export class LoginService {
    private heroesUrl1 = 'https://sandbox.apihub.citi.com/gcb/api/clientCredentials/oauth2/token/hk/gcb';  // URL to web api
    private heroesUrl2 = 'https://sandbox.apihub.citi.com/gcb/api/security/e2eKey';
    private heroesUrl3 = 'https://sandbox.apihub.citi.com/gcb/api/password/oauth2/token/hk/gcb';
    
    public accessToken: string = ''
    constructor(private http: HttpClient) { }

    loginStep1(): Promise<any> {
        let params = "grant_type=client_credentials&scope=/api"
        return this.http.post(this.heroesUrl1, params
            , httpOptions)
            .toPromise()
            .then(response => response)
            .catch(this.handleError);
    }

    loginStep2(accessToken): Observable<HttpResponse<any>> {
        let uuid = UUID.UUID();
        return this.http.get<any>(this.heroesUrl2
            , {
                headers: {
                    'content-type': 'application/json',
                    authorization: "Bearer " + accessToken,
                    client_id: "f2bdc852-3396-4b87-9e2e-4ef3ff83a983",
                    uuid: uuid
                }, observe: 'response'
            });
    }

    encryptPwd(password, eventid, modulus, exponent): string {
        var pub = new RSAKey();
        pub.setPublic(modulus, exponent);
        var encrypted_password;
        var unencrypted_data = eventid + ",b" + password;
        encrypted_password = pub.encryptB(getByteArray(unencrypted_data)).toString(16);
        return encrypted_password;
    }

    loginStep3(bizToken, username, password, eventid, modulus, exponent): Promise<any> {
        this.encryptPwd(password,eventid,modulus,exponent);
        let params = "grant_type=password&scope=/api&username=" + username + "&password=" + this.encryptPwd(password,eventid,modulus,exponent)
        let uuid = UUID.UUID();
        let options = {
            headers: new HttpHeaders({
                'content-type': 'application/x-www-form-urlencoded',
                authorization: "Basic " + encryptedKey,
                accept: 'application/json',
                uuid: uuid,
                bizToken: bizToken
            })
        }
        return this.http.post(this.heroesUrl3, params
            , options)
            .toPromise()
            .then(response => {
                this.accessToken = response['access_token'];
                return response;
            })
            .catch(this.handleError);
    }

    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }
}
