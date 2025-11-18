import { Injectable, NgZone } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
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
  private sseUrl = `${environment.apiUrl}/notifications/stream`;
  private eventSource: EventSource | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 2000; // 2 seconds

  private unreadCountSubject = new Subject<number>();
  private notificationsUpdatedSubject = new Subject<void>();
  private newNotificationSubject = new Subject<Notification>();

  unreadCount$ = this.unreadCountSubject.asObservable();
  notificationsUpdated$ = this.notificationsUpdatedSubject.asObservable();
  newNotification$ = this.newNotificationSubject.asObservable();

  constructor(
    private http: HttpClient,
    private ngZone: NgZone
  ) {
    // Start SSE connection for real-time updates
    this.connectToSSE();
  }

  /**
   * Connect to Server-Sent Events (SSE) stream for real-time notifications
   */
  private connectToSSE(): void {
    // Get auth token from localStorage (check both possible keys)
    const token = localStorage.getItem('auth_token') || localStorage.getItem('token');
    if (!token) {
      console.warn('No auth token found, cannot connect to SSE');
      return;
    }

    // EventSource doesn't support custom headers, so we pass token as query parameter
    // Backend JwtAuthenticationFilter has been updated to accept tokens from query params for SSE endpoints
    const sseUrlWithToken = `${this.sseUrl}?token=${encodeURIComponent(token)}`;

    this.ngZone.runOutsideAngular(() => {
      this.eventSource = new EventSource(sseUrlWithToken);

      this.eventSource.addEventListener('connected', (event: MessageEvent) => {
        this.ngZone.run(() => {
          console.log('SSE connected:', event.data);
          this.reconnectAttempts = 0; // Reset reconnect counter on successful connection
        });
      });

      this.eventSource.addEventListener('notification', (event: MessageEvent) => {
        this.ngZone.run(() => {
          try {
            const notification: Notification = JSON.parse(event.data);
            console.log('📩 SSE notification event received:', notification);
            this.newNotificationSubject.next(notification);
            this.notificationsUpdatedSubject.next();
            // Note: Don't call refreshUnreadCount() here - backend sends 'unread-count' event separately
          } catch (error) {
            console.error('❌ Error parsing notification:', error);
          }
        });
      });

      this.eventSource.addEventListener('unread-count', (event: MessageEvent) => {
        this.ngZone.run(() => {
          try {
            const data = JSON.parse(event.data);
            console.log('🔢 SSE unread-count event received:', data.unreadCount);
            this.unreadCountSubject.next(data.unreadCount);
          } catch (error) {
            console.error('❌ Error parsing unread count:', error);
          }
        });
      });

      this.eventSource.addEventListener('heartbeat', (event: MessageEvent) => {
        // Heartbeat received - connection is alive
        console.debug('SSE heartbeat received');
      });

      this.eventSource.onerror = (error) => {
        this.ngZone.run(() => {
          console.error('SSE connection error:', error);
          this.handleSSEError();
        });
      };
    });
  }

  /**
   * Handle SSE connection errors and attempt reconnection
   */
  private handleSSEError(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = this.reconnectDelay * this.reconnectAttempts;
      console.log(`Attempting to reconnect SSE (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}) in ${delay}ms...`);

      setTimeout(() => {
        this.connectToSSE();
      }, delay);
    } else {
      console.error('Max SSE reconnect attempts reached. Please refresh the page.');
    }
  }

  /**
   * Disconnect from SSE stream (call on logout or service destroy)
   */
  disconnectSSE(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      console.log('SSE connection closed');
    }
  }

  /**
   * Reconnect to SSE stream (call after login)
   */
  reconnectSSE(): void {
    this.disconnectSSE();
    this.reconnectAttempts = 0;
    this.connectToSSE();
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
