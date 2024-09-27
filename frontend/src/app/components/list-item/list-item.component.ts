import { Component, Input, OnChanges, SimpleChanges,  } from '@angular/core';
import { Record } from '../../models/record.model';
import { NgIf, NgFor } from '@angular/common';

@Component({
  selector: 'list-item',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './list-item.component.html',
  styleUrl: './list-item.component.css'
})
export class ListItemComponent implements OnChanges {
  @Input() item!: Record;
  notes: string[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['item'] && this.item) {
      this.modifyItem();
    }
  }

  modifyItem() {
    if (!this.item.notes) {
      this.item.notes = 'no data';
    }
    this.notes = this.item.notes.split('\n');
  }

  isVisible = false;

  toggleVisibility() {
    this.isVisible = !this.isVisible;
  }
}
