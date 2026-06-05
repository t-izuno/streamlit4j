import { registerComponent } from './component-registry';
import { StarRating } from './components/StarRating';

/**
 * First-party in-process components bundled with the streamlit4j SPA. Importing
 * this module triggers registration via {@link registerComponent}, so it MUST
 * be imported once at app startup (see {@code main.tsx}).
 *
 * <p>To add a new built-in component:
 *   1. create the React renderer in {@code ./components/<Name>.tsx}
 *   2. import it here and call {@code registerComponent('<name>', <Renderer>)}
 *      from {@code ./component-registry}
 *   3. ensure the Java side declares the matching {@code CustomComponent} and
 *      calls {@code St.registerComponent(...)}
 */
registerComponent('star-rating', StarRating);
