export class Task {
    id: number;
    description: string;
    completed: boolean;

    constructor(id: number, description: string, completed: boolean) {
        this.id = id;
        this.description = description;
        this.completed = completed;
    }
}
