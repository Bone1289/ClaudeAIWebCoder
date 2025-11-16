import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  features = [
    {
      title: 'Spring Boot Backend',
      description: 'RESTful API built with Spring Boot 3.3.5',
      icon: '🚀'
    },
    {
      title: 'Angular Frontend',
      description: 'Modern Angular 17 application with TypeScript',
      icon: '⚡'
    },
    {
      title: 'API Integration',
      description: 'Seamless communication between frontend and backend',
      icon: '🔗'
    },
    {
      title: 'Responsive Design',
      description: 'Mobile-friendly user interface',
      icon: '📱'
    }
  ];
}
