import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject, timer, EMPTY } from 'rxjs';
import { tap, map, switchMap, catchError } from 'rxjs/operators';
import { GrpcClientService } from '../grpc/grpc-client.service';

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
  private readonly SERVICE_NAME = 'com.example.demo.grpc.NotificationService';
  private readonly POLL_INTERVAL = 10000; // Poll every 10 seconds
  
  private unreadCountSubject = new Subject<number>();
  private notificationsUpdatedSubject = new Subject<void>();
  private newNotificationSubject = new Subject<Notification>();
  private pollingSubscription: any = null;

  unreadCount$ = this.unreadCountSubject.asObservable();
  notificationsUpdated$ = this.notificationsUpdatedSubject.asObservable();
  newNotification$ = this.newNotificationSubject.asObservable();

  constructor(
    private grpcClient: GrpcClientService,
    private ngZone: NgZone
  ) {
    // Start polling for real-time updates
    this.startPolling();
  }

  /**
   * Start polling for new notifications (replaces SSE)
   */
  private startPolling(): void {
    this.ngZone.runOutsideAngular(() => {
      this.pollingSubscription = timer(0, this.POLL_INTERVAL)
        .pipe(
          switchMap(() => this.refreshUnreadCount()),
          catchError(error => {
            console.error('Polling error:', error);
            return EMPTY;
          })
        )
        .subscribe();
    });
  }

  /**
   * Stop polling
   */
  private stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
  }

  /**
   * Disconnect from notification updates (call on logout)
   */
  disconnectSSE(): void {
    this.stopPolling();
    console.log('Notification polling stopped');
  }

  /**
   * Reconnect to notification updates (call after login)
   */
  reconnectSSE(): void {
    this.stopPolling();
    this.startPolling();
    console.log('Notification polling started');
  }

  /**
   * Get all notifications for current user (paginated)
   */
  getNotifications(page: number = 0, size: number = 20): Observable<ApiResponse<PagedNotifications>> {
    const grpcRequest = {
      pagination: {
        page: page,
        size: size,
        sort_by: 'createdAt',
        sort_direction: 'DESC'
      }
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAllNotifications',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: {
          content: response.notifications ? response.notifications.map((n: any) => this.mapGrpcNotificationToModel(n)) : [],
          totalElements: response.pagination?.total_elements || 0,
          totalPages: response.pagination?.total_pages || 0,
          size: response.pagination?.size || size,
          number: response.pagination?.page || page
        }
      }))
    );
  }

  /**
   * Get unread notifications
   */
  getUnreadNotifications(page: number = 0, size: number = 20): Observable<ApiResponse<PagedNotifications>> {
    const grpcRequest = {
      page: page,
      size: size,
      sort_by: 'createdAt',
      sort_direction: 'DESC'
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetUnreadNotifications',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: {
          content: response.notifications ? response.notifications.map((n: any) => this.mapGrpcNotificationToModel(n)) : [],
          totalElements: response.pagination?.total_elements || 0,
          totalPages: response.pagination?.total_pages || 0,
          size: response.pagination?.size || size,
          number: response.pagination?.page || page
        }
      }))
    );
  }

  /**
   * Get read notifications
   */
  getReadNotifications(page: number = 0, size: number = 20): Observable<ApiResponse<PagedNotifications>> {
    const grpcRequest = {
      page: page,
      size: size,
      sort_by: 'createdAt',
      sort_direction: 'DESC'
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetReadNotifications',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: {
          content: response.notifications ? response.notifications.map((n: any) => this.mapGrpcNotificationToModel(n)) : [],
          totalElements: response.pagination?.total_elements || 0,
          totalPages: response.pagination?.total_pages || 0,
          size: response.pagination?.size || size,
          number: response.pagination?.page || page
        }
      }))
    );
  }

  /**
   * Get recent notifications (last N days)
   */
  getRecentNotifications(days: number = 7): Observable<ApiResponse<Notification[]>> {
    const grpcRequest = {
      days: days
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetRecentNotifications',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.notifications ? response.notifications.map((n: any) => this.mapGrpcNotificationToModel(n)) : []
      }))
    );
  }

  /**
   * Get unread notification count
   */
  getUnreadCount(): Observable<number> {
    return this.grpcClient.call<{}, any>(
      this.SERVICE_NAME,
      'GetUnreadCount',
      {}
    ).pipe(
      map(response => Number(response.count || 0))
    );
  }

  /**
   * Refresh unread count and notify subscribers
   */
  refreshUnreadCount(): Observable<void> {
    return this.getUnreadCount().pipe(
      tap(count => {
        this.ngZone.run(() => {
          this.unreadCountSubject.next(count);
        });
      }),
      map(() => void 0)
    );
  }

  /**
   * Mark notification as read
   */
  markAsRead(notificationId: string): Observable<ApiResponse<Notification>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'MarkAsRead',
      { id: notificationId }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.notification ? this.mapGrpcNotificationToModel(response.notification) : {} as Notification
      } as ApiResponse<Notification>)),
      tap(() => {
        this.notificationsUpdatedSubject.next();
        this.refreshUnreadCount().subscribe();
      })
    );
  }

  /**
   * Mark notification as unread
   */
  markAsUnread(notificationId: string): Observable<ApiResponse<Notification>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'MarkAsUnread',
      { id: notificationId }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.notification ? this.mapGrpcNotificationToModel(response.notification) : {} as Notification
      } as ApiResponse<Notification>)),
      tap(() => {
        this.notificationsUpdatedSubject.next();
        this.refreshUnreadCount().subscribe();
      })
    );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<ApiResponse<{count: number}>> {
    return this.grpcClient.call<{}, any>(
      this.SERVICE_NAME,
      'MarkAllAsRead',
      {}
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: { count: response.updated_count || 0 }
      })),
      tap(() => {
        this.notificationsUpdatedSubject.next();
        this.refreshUnreadCount().subscribe();
      })
    );
  }

  /**
   * Delete a notification
   */
  deleteNotification(notificationId: string): Observable<ApiResponse<void>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'DeleteNotification',
      { id: notificationId }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: undefined
      })),
      tap(() => {
        this.notificationsUpdatedSubject.next();
        this.refreshUnreadCount().subscribe();
      })
    );
  }

  /**
   * Delete all notifications
   */
  deleteAllNotifications(): Observable<ApiResponse<{count: number}>> {
    return this.grpcClient.call<{}, any>(
      this.SERVICE_NAME,
      'DeleteAllNotifications',
      {}
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: { count: response.deleted_count || 0 }
      })),
      tap(() => {
        this.notificationsUpdatedSubject.next();
        this.refreshUnreadCount().subscribe();
      })
    );
  }

  /**
   * Clean up old notifications (older than N days)
   */
  cleanupOldNotifications(daysOld: number = 30): Observable<ApiResponse<{count: number}>> {
    const grpcRequest = {
      days_old: daysOld
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'CleanupOldNotifications',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: { count: response.deleted_count || 0 }
      })),
      tap(() => {
        this.notificationsUpdatedSubject.next();
      })
    );
  }

  /**
   * Map gRPC notification response to Angular model
   */
  private mapGrpcNotificationToModel(grpcNotification: any): Notification {
    return {
      id: grpcNotification.id,
      userId: grpcNotification.user_id,
      type: grpcNotification.type,
      channel: grpcNotification.channel,
      title: grpcNotification.title,
      message: grpcNotification.message,
      priority: grpcNotification.priority,
      read: grpcNotification.read,
      createdAt: grpcNotification.created_at || new Date().toISOString(),
      readAt: grpcNotification.read_at
    };
  }

  /**
   * Get icon for notification type
   */
  getTypeIcon(type: string): string {
    const iconMap: { [key: string]: string } = {
      'TRANSACTION_COMPLETED': 'check_circle',
      'SECURITY_ALERT': 'warning',
      'ACCOUNT_CREATED': 'account_circle',
      'ACCOUNT_UPDATED': 'edit',
      'DEPOSIT': 'arrow_downward',
      'WITHDRAWAL': 'arrow_upward',
      'TRANSFER': 'swap_horiz',
      'LOW_BALANCE': 'account_balance_wallet',
      'SYSTEM': 'info',
      'PAYMENT': 'payment'
    };
    return iconMap[type] || 'notifications';
  }

  /**
   * Get color for notification priority
   */
  getPriorityColor(priority: string): string {
    const colorMap: { [key: string]: string } = {
      'LOW': '#4CAF50',      // green
      'MEDIUM': '#2196F3',   // blue
      'HIGH': '#FF9800',     // orange
      'URGENT': '#F44336'    // red
    };
    return colorMap[priority] || '#757575'; // gray as default
  }
}
