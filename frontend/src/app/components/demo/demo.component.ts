import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-demo',
  templateUrl: './demo.component.html',
  styleUrls: ['./demo.component.css']
})
export class DemoComponent implements OnInit {
  name: string = '';
  helloResponse: string = '';
  healthResponse: string = '';
  loading: boolean = false;
  error: string = '';

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.checkHealth();
  }

  sayHello(): void {
    if (!this.name.trim()) {
      this.error = 'Please enter a name';
      return;
    }

    this.loading = true;
    this.error = '';

    this.apiService.getHello(this.name).subscribe({
      next: (response) => {
        this.helloResponse = response;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to fetch hello message: ' + err.message;
        this.loading = false;
      }
    });
  }

  checkHealth(): void {
    this.loading = true;
    this.error = '';

    this.apiService.getHealth().subscribe({
      next: (response) => {
        this.healthResponse = response;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to fetch health status: ' + err.message;
        this.healthResponse = 'Backend is not available';
        this.loading = false;
      }
    });
  }

  clearResponse(): void {
    this.helloResponse = '';
    this.name = '';
    this.error = '';
  }
}
