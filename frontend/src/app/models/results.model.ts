import { ArchivedResults } from "./archived.results.model";
import { Result } from "./result.model";

export interface Results {
    results: Result[];
    archives: ArchivedResults[];
}