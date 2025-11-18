import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService, Notification } from '../../services/notification.service';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  isOpen = false;
  unreadCount = 0;
  notifications: Notification[] = [];
  loading = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to unread count updates
    const unreadSub = this.notificationService.unreadCount$.subscribe({
      next: (count) => {
        console.log('🔔 Notification bell: Unread count updated from', this.unreadCount, 'to', count);
        this.unreadCount = count;
      },
      error: (error) => {
        console.error('Error getting unread count:', error);
      }
    });
    this.subscriptions.push(unreadSub);

    // Subscribe to new notifications (for real-time updates)
    const newNotificationSub = this.notificationService.newNotification$.subscribe({
      next: (notification) => {
        console.log('New notification received in bell component:', notification);
        // If dropdown is open, reload notifications to show the new one
        if (this.isOpen) {
          this.loadNotifications();
        }
      },
      error: (error) => {
        console.error('Error receiving new notification:', error);
      }
    });
    this.subscriptions.push(newNotificationSub);

    // Subscribe to notifications updated event
    const updateSub = this.notificationService.notificationsUpdated$.subscribe({
      next: () => {
        if (this.isOpen) {
          this.loadNotifications();
        }
      }
    });
    this.subscriptions.push(updateSub);

    // Initial load of unread count
    this.notificationService.refreshUnreadCount();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.loadNotifications();
    }
  }

  closeDropdown(): void {
    this.isOpen = false;
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.getUnreadNotifications(0, 10).subscribe({
      next: (response) => {
        if (response.success) {
          this.notifications = response.data.content;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
        this.loading = false;
      }
    });
  }

  markAsRead(notification: Notification, event: Event): void {
    event.stopPropagation();
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.read = true;
        },
        error: (error) => {
          console.error('Error marking notification as read:', error);
        }
      });
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
      },
      error: (error) => {
        console.error('Error marking all as read:', error);
      }
    });
  }

  deleteNotification(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.notificationService.deleteNotification(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
      },
      error: (error) => {
        console.error('Error deleting notification:', error);
      }
    });
  }

  viewAllNotifications(): void {
    this.closeDropdown();
    this.router.navigate(['/notifications']);
  }

  getTypeIcon(type: string): string {
    return this.notificationService.getTypeIcon(type);
  }

  getPriorityColor(priority: string): string {
    return this.notificationService.getPriorityColor(priority);
  }

  getTimeAgo(createdAt: string): string {
    const now = new Date();
    const created = new Date(createdAt);
    const diffMs = now.getTime() - created.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return created.toLocaleDateString();
  }
}
