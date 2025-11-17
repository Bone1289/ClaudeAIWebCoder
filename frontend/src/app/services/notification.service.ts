import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject, interval } from 'rxjs';
import { switchMap, tap, startWith } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Notification {
  id: string;
  userId: string;
  type: string;
  channel: string;
  title: string;
  message: string;
  priority: string;
  read: boolean;
  createdAt: string;
  readAt?: string;
}

export interface PagedNotifications {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  private unreadCountSubject = new Subject<number>();
  private notificationsUpdatedSubject = new Subject<void>();

  // Polling interval in milliseconds (30 seconds)
  private pollingInterval = 30000;

  unreadCount$ = this.unreadCountSubject.asObservable();
  notificationsUpdated$ = this.notificationsUpdatedSubject.asObservable();

  constructor(private http: HttpClient) {
    // Start polling for unread count
    this.startPolling();
  }

  /**
   * Start polling for unread notification count
   */
  private startPolling(): void {
    interval(this.pollingInterval)
      .pipe(
        startWith(0), // Emit immediately
        switchMap(() => this.getUnreadCount())
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.unreadCountSubject.next(response.data);
          }
        },
        error: (error) => {
          console.error('Error polling unread count:', error);
        }
      });
  }

  /**
   * Get all notifications for current user (paginated)
   */
  getNotifications(page: number = 0, size: number = 20): Observable<ApiResponse<PagedNotifications>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PagedNotifications>>(this.apiUrl, { params });
  }

  /**
   * Get unread notifications
   */
  getUnreadNotifications(page: number = 0, size: number = 20): Observable<ApiResponse<PagedNotifications>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PagedNotifications>>(`${this.apiUrl}/unread`, { params });
  }

  /**
   * Get read notifications
   */
  getReadNotifications(page: number = 0, size: number = 20): Observable<ApiResponse<PagedNotifications>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PagedNotifications>>(`${this.apiUrl}/read`, { params });
  }

  /**
   * Get recent notifications (last N days)
   */
  getRecentNotifications(days: number = 7, page: number = 0, size: number = 20): Observable<ApiResponse<PagedNotifications>> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PagedNotifications>>(`${this.apiUrl}/recent`, { params });
  }

  /**
   * Get unread notification count
   */
  getUnreadCount(): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.apiUrl}/unread-count`);
  }

  /**
   * Get a specific notification by ID
   */
  getNotification(id: string): Observable<ApiResponse<Notification>> {
    return this.http.get<ApiResponse<Notification>>(`${this.apiUrl}/${id}`);
  }

  /**
   * Mark notification as read
   */
  markAsRead(id: string): Observable<ApiResponse<Notification>> {
    return this.http.put<ApiResponse<Notification>>(`${this.apiUrl}/${id}/read`, {})
      .pipe(
        tap(() => {
          this.refreshUnreadCount();
          this.notificationsUpdatedSubject.next();
        })
      );
  }

  /**
   * Mark notification as unread
   */
  markAsUnread(id: string): Observable<ApiResponse<Notification>> {
    return this.http.put<ApiResponse<Notification>>(`${this.apiUrl}/${id}/unread`, {})
      .pipe(
        tap(() => {
          this.refreshUnreadCount();
          this.notificationsUpdatedSubject.next();
        })
      );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<ApiResponse<number>> {
    return this.http.put<ApiResponse<number>>(`${this.apiUrl}/mark-all-read`, {})
      .pipe(
        tap(() => {
          this.refreshUnreadCount();
          this.notificationsUpdatedSubject.next();
        })
      );
  }

  /**
   * Delete a notification
   */
  deleteNotification(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`)
      .pipe(
        tap(() => {
          this.refreshUnreadCount();
          this.notificationsUpdatedSubject.next();
        })
      );
  }

  /**
   * Delete all notifications
   */
  deleteAllNotifications(): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/all`)
      .pipe(
        tap(() => {
          this.refreshUnreadCount();
          this.notificationsUpdatedSubject.next();
        })
      );
  }

  /**
   * Delete old read notifications (cleanup)
   */
  cleanupOldNotifications(daysOld: number = 30): Observable<ApiResponse<number>> {
    const params = new HttpParams().set('daysOld', daysOld.toString());

    return this.http.delete<ApiResponse<number>>(`${this.apiUrl}/cleanup`, { params })
      .pipe(
        tap(() => {
          this.notificationsUpdatedSubject.next();
        })
      );
  }

  /**
   * Manually refresh unread count
   */
  refreshUnreadCount(): void {
    this.getUnreadCount().subscribe({
      next: (response) => {
        if (response.success) {
          this.unreadCountSubject.next(response.data);
        }
      },
      error: (error) => {
        console.error('Error refreshing unread count:', error);
      }
    });
  }

  /**
   * Get priority color class
   */
  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'URGENT':
        return 'text-red-600';
      case 'HIGH':
        return 'text-orange-600';
      case 'MEDIUM':
        return 'text-yellow-600';
      case 'LOW':
        return 'text-green-600';
      default:
        return 'text-gray-600';
    }
  }

  /**
   * Get notification type icon
   */
  getTypeIcon(type: string): string {
    switch (type) {
      case 'ACCOUNT_CREATED':
        return '🎉';
      case 'TRANSACTION_COMPLETED':
        return '✅';
      case 'TRANSACTION_FAILED':
        return '❌';
      case 'SECURITY_ALERT':
        return '🔒';
      case 'SYSTEM_ANNOUNCEMENT':
        return '📢';
      case 'ACCOUNT_SUSPENDED':
        return '⚠️';
      case 'ACCOUNT_ACTIVATED':
        return '✅';
      default:
        return '📬';
    }
  }
}
