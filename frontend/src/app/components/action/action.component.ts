import { Component, Input, OnChanges, SimpleChanges,  } from '@angular/core';
import { Record } from '../../models/record.model';
import { NgIf, NgFor } from '@angular/common';

@Component({
  selector: 'action',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './action.component.html',
  styleUrl: './action.component.css'
})
export class ActionComponent implements OnChanges {
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
