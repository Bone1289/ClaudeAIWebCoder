import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject, map } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Apollo } from 'apollo-angular';
import { environment } from '../../environments/environment';
import {
  GET_NOTIFICATIONS,
  GET_UNREAD_NOTIFICATIONS,
  GET_UNREAD_COUNT,
  MARK_AS_READ,
  MARK_ALL_AS_READ,
  DELETE_NOTIFICATION,
  DELETE_ALL_NOTIFICATIONS
} from '../graphql/graphql.operations';

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
  notifications: Notification[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
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
    private apollo: Apollo,
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

    // EventSource doesn't support custom headers, so we need to pass token as query param or cookie
    // For now, we'll rely on the cookie-based auth
    this.ngZone.runOutsideAngular(() => {
      this.eventSource = new EventSource(this.sseUrl, { withCredentials: true });

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
  getNotifications(page: number = 0, size: number = 20): Observable<PagedNotifications> {
    return this.apollo.query({
      query: GET_NOTIFICATIONS,
      variables: { page, size },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).notifications)
    );
  }

  /**
   * Get unread notifications
   */
  getUnreadNotifications(page: number = 0, size: number = 20): Observable<PagedNotifications> {
    return this.apollo.query({
      query: GET_UNREAD_NOTIFICATIONS,
      variables: { page, size },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).unreadNotifications)
    );
  }

  /**
   * Get read notifications
   */
  getReadNotifications(page: number = 0, size: number = 20): Observable<PagedNotifications> {
    return this.apollo.query({
      query: GET_NOTIFICATIONS,
      variables: { page, size },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).readNotifications)
    );
  }

  /**
   * Get recent notifications (last N days)
   */
  getRecentNotifications(days: number = 7, page: number = 0, size: number = 20): Observable<PagedNotifications> {
    return this.apollo.query({
      query: GET_NOTIFICATIONS,
      variables: { days, page, size },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).recentNotifications)
    );
  }

  /**
   * Get unread notification count
   */
  getUnreadCount(): Observable<number> {
    return this.apollo.query({
      query: GET_UNREAD_COUNT,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).unreadCount)
    );
  }

  /**
   * Get a specific notification by ID
   */
  getNotification(id: string): Observable<Notification> {
    return this.apollo.query({
      query: GET_NOTIFICATIONS,
      variables: { id },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).notification)
    );
  }

  /**
   * Mark notification as read
   */
  markAsRead(id: string): Observable<Notification> {
    return this.apollo.mutate({
      mutation: MARK_AS_READ,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).markNotificationAsRead),
      tap(() => {
        this.refreshUnreadCount();
        this.notificationsUpdatedSubject.next();
      })
    );
  }

  /**
   * Mark notification as unread
   */
  markAsUnread(id: string): Observable<Notification> {
    return this.apollo.mutate({
      mutation: MARK_AS_READ,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).markNotificationAsUnread),
      tap(() => {
        this.refreshUnreadCount();
        this.notificationsUpdatedSubject.next();
      })
    );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<boolean> {
    return this.apollo.mutate({
      mutation: MARK_ALL_AS_READ
    }).pipe(
      map(result => (result.data as any).markAllNotificationsAsRead),
      tap(() => {
        this.refreshUnreadCount();
        this.notificationsUpdatedSubject.next();
      })
    );
  }

  /**
   * Delete a notification
   */
  deleteNotification(id: string): Observable<boolean> {
    return this.apollo.mutate({
      mutation: DELETE_NOTIFICATION,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).deleteNotification),
      tap(() => {
        this.refreshUnreadCount();
        this.notificationsUpdatedSubject.next();
      })
    );
  }

  /**
   * Delete all notifications
   */
  deleteAllNotifications(): Observable<boolean> {
    return this.apollo.mutate({
      mutation: DELETE_ALL_NOTIFICATIONS
    }).pipe(
      map(result => (result.data as any).deleteAllNotifications),
      tap(() => {
        this.refreshUnreadCount();
        this.notificationsUpdatedSubject.next();
      })
    );
  }

  /**
   * Delete old read notifications (cleanup)
   */
  cleanupOldNotifications(daysOld: number = 30): Observable<number> {
    return this.apollo.mutate({
      mutation: DELETE_ALL_NOTIFICATIONS,
      variables: { daysOld }
    }).pipe(
      map(result => (result.data as any).cleanupOldNotifications),
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
      next: (count) => {
        this.unreadCountSubject.next(count);
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
