import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule }    from '@angular/forms';
import { HttpClientModule }    from '@angular/common/http';

import { AppRoutingModule }     from './app-routing.module';

import { AppComponent } from './app.component';
import { LoginComponent }   from './login/login.component';
import { AccountSummaryComponent }   from './account-summary/account-summary.component';
import { LoginService }          from './login/services/login.service';
import { AccountSummaryService } from './account-summary/services/account-summary.service';

import { AccordionModule } from 'ngx-bootstrap';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    AccountSummaryComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    AccordionModule.forRoot()
  ],
  providers: [ LoginService, AccountSummaryService ],
  bootstrap: [AppComponent]
})
export class AppModule { }
